package edu.internet2.middleware.grouper.dictionary;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * dao for dictionaries
 * @author mchyzer
 *
 */
public class GrouperDictionaryDao {


  public GrouperDictionaryDao() {
  }

  /**
   * @param grouperDictionary
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDictionary grouperDictionary) {
    
    GrouperUtil.assertion(grouperDictionary != null, "grouperDictionary is null");
    
    boolean isInsert = grouperDictionary.getInternalId() == -1;

    grouperDictionary.storePrepare();

    if (!isInsert) {
      boolean changed = new GcDbAccess().storeToDatabase(grouperDictionary);
      return changed;
    }

    RuntimeException runtimeException = null;
    // might be other places saving the same dictionary
    for (int i=0;i<5;i++) {
      try {
        new GcDbAccess().storeToDatabase(grouperDictionary);
        return true;
      } catch (RuntimeException re) {
        runtimeException = re;
        GrouperUtil.sleep(100 * (i+1));
        GrouperDictionary grouperDictionaryNew = selectByText(grouperDictionary.getTheText());
        if (grouperDictionaryNew != null) {
          return false;
        }
        if (i==4) {
          throw re;
        }
      }
    }
    // this should never happen :)
    throw runtimeException;
  }  

  public static GrouperDictionary selectByText(String theText) {
    if (StringUtils.isBlank(theText)) {
      return null;
    }
    GrouperDictionary grouperDictionary = new GcDbAccess().sql("select * from grouper_dictionary where the_text = ?")
        .addBindVar(theText).select(GrouperDictionary.class);
    return grouperDictionary;
  }
  
  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDictionary grouperDictionary) {
    grouperDictionary.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDictionary);
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDictionary.class)).executeSql();
  }

  /**
   * cache, use the method to get this
   */
  private static ExpirableCache<String, Long> textToInternalIdCache = null;
  
  /**
   * max terms in memory
   */
  private static int maxTermsInMemoryCache = 50000;
  
  /**
   * dictionary cache
   * @return the cache
   */
  private static ExpirableCache<String, Long> textToInternalIdCache() {
    if (textToInternalIdCache == null) {
      maxTermsInMemoryCache = GrouperConfig.retrieveConfig().propertyValueInt("grouper.dictionary.maxTermsInMemoryCache ", 50000);
      int cacheStoreForMinutes = GrouperConfig.retrieveConfig().propertyValueInt("grouper.dictionary.cacheStoreForMinutes");
      textToInternalIdCache = new ExpirableCache<String, Long>(cacheStoreForMinutes);
    }
    return textToInternalIdCache;
  }
  
  /**
   * cache, use the method to get this
   */
  private static ExpirableCache<Long, String> internalIdToTextCache = null;
  
  /**
   * dictionary cache
   * @return the cache
   */
  private static ExpirableCache<Long, String> internalIdToTextCache() {
    if (internalIdToTextCache == null) {
      int cacheStoreForMinutes = GrouperConfig.retrieveConfig().propertyValueInt("grouper.dictionary.cacheStoreForMinutes");
      internalIdToTextCache = new ExpirableCache<Long, String>(cacheStoreForMinutes);
    }
    return internalIdToTextCache;
  }
  
  /**
   * @param string
   * @return the dictionary
   */
  public static Long findOrAdd(String string) {
    if (StringUtils.isBlank(string)) {
      return null;
    }

    Long internalId = textToInternalIdCache().get(string);
    if (internalId == null) {
      GrouperDictionary grouperDictionary = new GrouperDictionary();
      grouperDictionary.setTheText(string);
      store(grouperDictionary);
      internalId = grouperDictionary.getInternalId();
      
      if (textToInternalIdCache().size(false) < maxTermsInMemoryCache) {
        textToInternalIdCache().put(string, internalId);
        internalIdToTextCache().put(internalId, string);
      }
    }
    return internalId;
  }
  

}
