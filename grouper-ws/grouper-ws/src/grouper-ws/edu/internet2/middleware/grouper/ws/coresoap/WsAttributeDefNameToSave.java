/**
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefNameAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.AttributeDefNameAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;



/**
 * <pre>
 * Class to save an attribute def name via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsAttributeDefNameToSave {

  /** attribute def name lookup (blank if insert) */
  private WsAttributeDefNameLookup wsAttributeDefNameLookup;

  /** attribute def name to save */
  private WsAttributeDefName wsAttributeDefName;

  /** T or F (null if F) */
  private String createParentStemsIfNotExist;
  
  /**
   * if should create parent stems if not exist
   * @return T or F or null (F)
   */
  public String getCreateParentStemsIfNotExist() {
    return this.createParentStemsIfNotExist;
  }

  /**
   * if should create parent stems if not exist
   * @param createParentStemsIfNotExist1 T or F or null (F)
   */
  public void setCreateParentStemsIfNotExist(String createParentStemsIfNotExist1) {
    this.createParentStemsIfNotExist = createParentStemsIfNotExist1;
  }

  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;

  /**
   * what ended up happening
   */
  @XStreamOmitField
  private SaveResultType saveResultType;

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsAttributeDefNameToSave.class);

  /**
   * 
   */
  public WsAttributeDefNameToSave() {
    // empty constructor
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @return the saveMode
   */
  public String getSaveMode() {
    return this.saveMode;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param saveMode1 the saveMode to set
   */
  public void setSaveMode(String saveMode1) {
    this.saveMode = saveMode1;
  }

  /**
   * attribute def name lookup (blank if insert)
   * @return the wsAttributeDefNameLookup
   */
  public WsAttributeDefNameLookup getWsAttributeDefNameLookup() {
    return this.wsAttributeDefNameLookup;
  }

  /**
   * attribute def name lookup (blank if insert)
   * @param wsAttributeDefNameLookup1 the wsAttributeDefNameLookup to set
   */
  public void setWsAttributeDefNameLookup(WsAttributeDefNameLookup wsAttributeDefNameLookup1) {
    this.wsAttributeDefNameLookup = wsAttributeDefNameLookup1;
  }

  /**
   * attribute def name to save
   * @return the wsAttributeDefName
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * attribute def name to save
   * @param wsAttributeDefName1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }

  /**
   * save this attributeDefName
   * 
   * @param grouperSession
   *            to save
   * @return the stem that was inserted or updated
   * @throws StemNotFoundException 
   * @throws StemAddException 
   * @throws AttributeDefNameAddException
   * @throws InsufficientPrivilegeException
   * @throws AttributeDefNameAddAlreadyExistsException
   */
  public AttributeDefName save(GrouperSession grouperSession) {
  
    AttributeDefName attributeDefName = null;
      
    try {
      SaveMode theSaveMode = SaveMode.valueOfIgnoreCase(this.saveMode);
  
      if (SaveMode.INSERT != theSaveMode && this.getWsAttributeDefNameLookup() == null) {
        throw new WsInvalidQueryException(
            "wsAttributeDefNameLookup is required to save an attributeDefName (probably just put the name in it)");
      }
       
      if (this.getWsAttributeDefNameLookup() == null) {
        this.setWsAttributeDefNameLookup(new WsAttributeDefNameLookup());
      }
       
      this.getWsAttributeDefNameLookup().retrieveAttributeDefNameIfNeeded(grouperSession);
  
      AttributeDefName attributeDefNameLookedup = this.getWsAttributeDefNameLookup().retrieveAttributeDefName();
  
      String attributeDefNameLookup = attributeDefNameLookedup == null ? null : attributeDefNameLookedup.getName();
  
      AttributeDef attributeDef = null;
      
      //we need the attribute definition, find it by id if was passed in
      if (!StringUtils.isBlank(this.getWsAttributeDefName().getAttributeDefId())) {
        attributeDef = AttributeDefFinder.findById(this.getWsAttributeDefName().getAttributeDefId(), false);
        
        if (attributeDef == null) {
          throw new WsInvalidQueryException("Cant find attributeDef by id: " + this.getWsAttributeDefName().getAttributeDefId());
        }
        
        //make sure the name matches
        if (!StringUtils.isBlank(this.getWsAttributeDefName().getAttributeDefName()) && !StringUtils.equals(this.getWsAttributeDefName().getAttributeDefName(), attributeDef.getName())) {
          throw new WsInvalidQueryException("AttributeDef for id: " + attributeDef.getUuid() + " has name: " + attributeDef.getName() 
              + ", but you passed in a different name: " + this.getWsAttributeDefName().getAttributeDefName());
        }
        
      } else if (!StringUtils.isBlank(this.getWsAttributeDefName().getAttributeDefName())) {

        attributeDef = AttributeDefFinder.findByName(this.getWsAttributeDefName().getAttributeDefName(), false);
        
        if (attributeDef == null) {
          throw new WsInvalidQueryException("Cant find attributeDef by name: " + this.getWsAttributeDefName().getAttributeDefName());
        }
        
      } else {
        throw new WsInvalidQueryException(
          "You need to pass in an attributeDefId or attributeDefName!");
      }
      
      AttributeDefNameSave attributeDefNameSave = new AttributeDefNameSave(grouperSession, attributeDef);
      attributeDefNameSave.assignAttributeDefNameNameToEdit(attributeDefNameLookup);
      attributeDefNameSave.assignUuid(this.getWsAttributeDefName().getUuid()).assignName(this.getWsAttributeDefName().getName());
      attributeDefNameSave.assignDisplayExtension(this.getWsAttributeDefName().getDisplayExtension());
      attributeDefNameSave.assignDescription(this.getWsAttributeDefName().getDescription());
      attributeDefNameSave.assignSaveMode(theSaveMode);
      attributeDefNameSave.assignCreateParentStemsIfNotExist(GrouperUtil.booleanValue(this.getCreateParentStemsIfNotExist(), false));
      
      attributeDefName = attributeDefNameSave.save();
      
      this.saveResultType = attributeDefNameSave.getSaveResultType();
  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return attributeDefName;
  }

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType saveResultType() {
    return this.saveResultType;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * validate the settings (e.g. that booleans are set correctly)
   */
  public void validate() {
    try {
      if (!StringUtils.isBlank(this.saveMode)) {
        //make sure it exists
        SaveMode.valueOfIgnoreCase(this.saveMode);
      }
    } catch (RuntimeException e) {
      throw new WsInvalidQueryException("Problem with save mode: " + e.getMessage()
          + ", " + this, e);
    }
  }

}
