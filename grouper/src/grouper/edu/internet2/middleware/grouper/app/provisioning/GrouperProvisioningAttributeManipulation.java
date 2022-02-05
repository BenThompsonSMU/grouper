package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;

public class GrouperProvisioningAttributeManipulation {

  public static final String DEFAULT_VALUE_EMPTY_STRING_CONFIG = "<emptyString>";
  
  public GrouperProvisioningAttributeManipulation() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  public GcGrouperSync getGcGrouperSync() {
    return this.getGrouperProvisioner().getGcGrouperSync();
  }
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * provisioner can decide to convert all nulls to empty
   * @return if convert nulls to empty
   */
  public boolean isConvertNullValuesToEmpty() {
    return false;
  }
  
  public void manipulateAttributesGroups(List<ProvisioningGroup> provisioningGroups) {
    
    int[] manipulateAttributesGroupsCount = new int[] {0};
    int[] convertNullsEmptyCount = new int[] {0};

    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values() ) {
        manipulateValue(provisioningGroup, grouperProvisioningConfigurationAttribute, manipulateAttributesGroupsCount);
        convertNullsEmpties(provisioningGroup, grouperProvisioningConfigurationAttribute, convertNullsEmptyCount);
      }
    }
    if (manipulateAttributesGroupsCount[0] > 0) {
      manipulateAttributesGroupsCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("manipulateAttributesGroupsCount"), 0);
      this.grouperProvisioner.getDebugMap().put("manipulateAttributesGroupsCount", manipulateAttributesGroupsCount[0]);
      
    }
    if (convertNullsEmptyCount[0] > 0) {
      convertNullsEmptyCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("convertNullsEmptyCount"), 0);
      this.grouperProvisioner.getDebugMap().put("convertNullsEmptyCount", convertNullsEmptyCount[0]);
      
    }

  }

  public void manipulateAttributesEntities(List<ProvisioningEntity> provisioningEntities) {
    
    int[] manipulateAttributesEntitiesCount = new int[] {0};
    int[] convertNullsEmptyCount = new int[] {0};

    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values() ) {
        manipulateValue(provisioningEntity, grouperProvisioningConfigurationAttribute, manipulateAttributesEntitiesCount);
        convertNullsEmpties(provisioningEntity, grouperProvisioningConfigurationAttribute, convertNullsEmptyCount);
      }
    }
    if (manipulateAttributesEntitiesCount[0] > 0) {
      manipulateAttributesEntitiesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("manipulateAttributesEntitiesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("manipulateAttributesEntitiesCount", manipulateAttributesEntitiesCount[0]);
      
    }
    if (convertNullsEmptyCount[0] > 0) {
      convertNullsEmptyCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("convertNullsEmptyCount"), 0);
      this.grouperProvisioner.getDebugMap().put("convertNullsEmptyCount", convertNullsEmptyCount[0]);
      
    }
  }

  public void manipulateAttributesMemberships(List<ProvisioningMembership> provisioningMemberships) {
    
    int[] manipulateAttributesMembershipsCount = new int[] {0};
    int[] convertNullsEmptyCount = new int[] {0};

    Map<String, GrouperProvisioningConfigurationAttribute> membershipAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : membershipAttributeNameToConfig.values() ) {
        manipulateValue(provisioningMembership, grouperProvisioningConfigurationAttribute, manipulateAttributesMembershipsCount);
        convertNullsEmpties(provisioningMembership, grouperProvisioningConfigurationAttribute, convertNullsEmptyCount);
      }
    }
    if (manipulateAttributesMembershipsCount[0] > 0) {
      manipulateAttributesMembershipsCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("manipulateAttributesMembershipsCount"), 0);
      this.grouperProvisioner.getDebugMap().put("manipulateAttributesMembershipsCount", manipulateAttributesMembershipsCount[0]);
      
    }
    if (convertNullsEmptyCount[0] > 0) {
      convertNullsEmptyCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("convertNullsEmptyCount"), 0);
      this.grouperProvisioner.getDebugMap().put("convertNullsEmptyCount", convertNullsEmptyCount[0]);
      
    }
  }

  /**
   * @param provisioningGroups
   * @param attribute null for all or an attribute name for a specific one
   */
  public void assignDefaultsForGroups(List<ProvisioningGroup> provisioningGroups, GrouperProvisioningConfigurationAttribute attribute) {
    
    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> groupFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig();

    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeId = groupFieldNameToConfig.get("id");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeDisplayName = groupFieldNameToConfig.get("displayName");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeName = groupFieldNameToConfig.get("name");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeIdIndex = groupFieldNameToConfig.get("idIndex");

    int[] assignDefaultFieldsAndAttributesCount = new int[] {0};
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeId) {
        provisioningGroup.setId((String)assignDefaultField(provisioningGroup.getId(), grouperProvisioningConfigurationAttributeId,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeDisplayName) {
        provisioningGroup.setDisplayName((String)assignDefaultField(provisioningGroup.getDisplayName(), grouperProvisioningConfigurationAttributeDisplayName,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeName) {
        provisioningGroup.setName((String)assignDefaultField(provisioningGroup.getName(), grouperProvisioningConfigurationAttributeName,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeIdIndex) {
        provisioningGroup.setIdIndex(GrouperUtil.longObjectValue(assignDefaultField(provisioningGroup.getIdIndex(), grouperProvisioningConfigurationAttributeIdIndex, assignDefaultFieldsAndAttributesCount), true));
      }
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values() ) {
        if (attribute == null || attribute == grouperProvisioningConfigurationAttribute) {
          assignDefault(provisioningGroup, grouperProvisioningConfigurationAttribute, assignDefaultFieldsAndAttributesCount);
        }
      }
    }
    if (assignDefaultFieldsAndAttributesCount[0] > 0) {
      assignDefaultFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("assignDefaultFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("assignDefaultFieldsAndAttributesCount", assignDefaultFieldsAndAttributesCount[0]);
    }
  }

  /**
   * @param provisioningEntities
   * @param attribute null for all or an attribute name for a specific one
   */
  public void assignDefaultsForEntities(List<ProvisioningEntity> provisioningEntities, GrouperProvisioningConfigurationAttribute attribute) {
    
    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> entityFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig();
    
    int[] assignDefaultFieldsAndAttributesCount = new int[] {0};
    
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeId = entityFieldNameToConfig.get("id");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeLoginId = entityFieldNameToConfig.get("loginId");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeName = entityFieldNameToConfig.get("name");
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeEmail = entityFieldNameToConfig.get("email");

    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeId) {
        provisioningEntity.setId((String)assignDefaultField(provisioningEntity.getId(), grouperProvisioningConfigurationAttributeId,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeLoginId) {
        provisioningEntity.setLoginId((String)assignDefaultField(provisioningEntity.getLoginId(), grouperProvisioningConfigurationAttributeLoginId,  assignDefaultFieldsAndAttributesCount));
      }
      
      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeName) {
        provisioningEntity.setName((String)assignDefaultField(provisioningEntity.getName(), grouperProvisioningConfigurationAttributeName,  assignDefaultFieldsAndAttributesCount));
      }

      if (attribute == null || attribute == grouperProvisioningConfigurationAttributeEmail) {
        provisioningEntity.setEmail((String)assignDefaultField(provisioningEntity.getEmail(), grouperProvisioningConfigurationAttributeEmail, assignDefaultFieldsAndAttributesCount));
      }

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values() ) {
        if (attribute == null || attribute == grouperProvisioningConfigurationAttribute) {
          assignDefault(provisioningEntity, grouperProvisioningConfigurationAttribute, assignDefaultFieldsAndAttributesCount);
        }
      }
    }
    if (assignDefaultFieldsAndAttributesCount[0] > 0) {
      assignDefaultFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("assignDefaultFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("assignDefaultFieldsAndAttributesCount", assignDefaultFieldsAndAttributesCount[0]);
    }
  }

  public void assignDefaultsForMemberships(List<ProvisioningMembership> provisioningMemberships) {
    
    Map<String, GrouperProvisioningConfigurationAttribute> membershipAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> membershipFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig();
    
    int[] assignDefaultFieldsAndAttributesCount = new int[] {0};
    
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      
      provisioningMembership.setId((String)assignDefaultField(provisioningMembership.getId(), membershipFieldNameToConfig.get("id"),  assignDefaultFieldsAndAttributesCount));
      provisioningMembership.setProvisioningEntityId((String)assignDefaultField(provisioningMembership.getProvisioningEntityId(), membershipFieldNameToConfig.get("provisioningEntityId"),  assignDefaultFieldsAndAttributesCount));
      provisioningMembership.setProvisioningGroupId((String)assignDefaultField(provisioningMembership.getProvisioningGroupId(), membershipFieldNameToConfig.get("provisioningGroupId"),  assignDefaultFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : membershipAttributeNameToConfig.values() ) {
        assignDefault(provisioningMembership, grouperProvisioningConfigurationAttribute, assignDefaultFieldsAndAttributesCount);
      }
    }
    if (assignDefaultFieldsAndAttributesCount[0] > 0) {
      assignDefaultFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("assignDefaultFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("assignDefaultFieldsAndAttributesCount", assignDefaultFieldsAndAttributesCount[0]);
    }
  }

  /**
   * 
   * @param currentValue
   * @param grouperProvisioningConfigurationAttribute
   * @param assignDefaultFieldsAndAttributesCount
   * @return return the current or new field
   */
  public Object assignDefaultField(Object currentValue, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      int[] assignDefaultFieldsAndAttributesCount) {

    if (grouperProvisioningConfigurationAttribute != null) {

      // set a default value if blank and is a grouper object
      if (currentValue == null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
        assignDefaultFieldsAndAttributesCount[0]++;
        if (grouperProvisioningConfigurationAttribute.getDefaultValue().equals(DEFAULT_VALUE_EMPTY_STRING_CONFIG)) {
          return "";
        }
        
        return grouperProvisioningConfigurationAttribute.getDefaultValue();
      }
    }

    return currentValue;
  }

  public void assignDefault(ProvisioningUpdatable provisioningUpdatable,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, int[] count) {

    if (grouperProvisioningConfigurationAttribute == null || StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
      return;
    }
    
    String defaultValue = grouperProvisioningConfigurationAttribute.getDefaultValue();
    if (StringUtils.equals(defaultValue, DEFAULT_VALUE_EMPTY_STRING_CONFIG)) {
      defaultValue = "";
    }

    Object currentValue = provisioningUpdatable.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());
    
    // scalar
    if (!grouperProvisioningConfigurationAttribute.isMultiValued()) {
      if (currentValue == null) {
        provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), defaultValue);
        count[0]++;
      }
      return;
    }

    if (currentValue == null) {
      count[0]++;
      provisioningUpdatable.addAttributeValue(grouperProvisioningConfigurationAttribute.getName(), defaultValue);
      return;
    }
    if (currentValue instanceof Collection) {
      Collection currentValueCollection = (Collection)currentValue;
      if (currentValueCollection.size() == 0) {
        currentValueCollection.add(defaultValue);
        count[0]++;
      }
      return;
    }
    if (currentValue != null && currentValue.getClass().isArray()) {
      if (Array.getLength(currentValue) == 0) {
        provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), new Object[] {defaultValue});
        count[0]++;
      }
      return;
    }
    throw new RuntimeException("Not expecting attribute type: " + currentValue.getClass());
  }

  public void manipulateValue(ProvisioningUpdatable provisioningUpdatable,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, int[] count) {

    if (grouperProvisioningConfigurationAttribute == null) {
      return;
    }

    Object currentValue = provisioningUpdatable.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());
    Object originalValue = currentValue;

    if (currentValue == null) {
      return;
    }
    GrouperProvisioningConfigurationAttributeValueType valueType = grouperProvisioningConfigurationAttribute.getValueType();
    if (valueType == null) {
      return;
    }
    
    if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
      if (currentValue instanceof Set) {
        // we good
      } else if (currentValue instanceof Collection) {
        currentValue = new HashSet((Collection)currentValue);
        
      } else if (currentValue.getClass().isArray()) {
        Set newValue = new HashSet();
        for (int i=0;i<Array.getLength(currentValue); i++) {
          newValue.add(Array.get(currentValue, i));
        }
        currentValue = newValue;
      } else {
        currentValue = GrouperUtil.toSet(currentValue);
      }
      if (!valueType.correctTypeForSet((Set)currentValue)) {
        Set newValue = new HashSet();
        for (Object value : (Set)currentValue) {
          newValue.add(valueType.convert(value));
        }
        currentValue = newValue;
      }
      if (originalValue != currentValue && count!= null) {
        count[0]++;
      }
      provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), currentValue);
      return;
    }
    // unwrap collections?
    if (currentValue instanceof Collection) {
      Collection collection = (Collection)currentValue;
      if (collection.size() == 1) {
        currentValue = collection.iterator().next();
      } else if (collection.size() == 0) {
        return;
      } else {
        throw new RuntimeException("Attribute should not be a collection: " + grouperProvisioningConfigurationAttribute.getName());
      }
    } else if (currentValue.getClass().isArray()) {
      if (Array.getLength(currentValue) == 1) {
        currentValue = Array.get(currentValue, 0);
      } else if (Array.getLength(currentValue) == 0) {
        return;
      } else {
        throw new RuntimeException("Attribute should not be an array: " + grouperProvisioningConfigurationAttribute.getName());
      }
    }
    Object newValue = valueType.convert(currentValue);
    if (originalValue != newValue && count!= null) {
      count[0]++;
    }
    provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), newValue);
  }

  /**
   * if the provisioner should equate nulls and empties, then convert nulls to empties
   * @param provisioningUpdatable
   * @param grouperProvisioningConfigurationAttribute
   * @param count
   */
  public void convertNullsEmpties(ProvisioningUpdatable provisioningUpdatable,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, int[] count) {

    // should we convert null to empty?
    if (!this.isConvertNullValuesToEmpty()) {
      return;
    }

    // not sure what this is
    if (grouperProvisioningConfigurationAttribute == null) {
      return;
    }

    Object currentValue = provisioningUpdatable.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());

    // its notnull so ignore
    if (currentValue != null) {
      return;
    }
    GrouperProvisioningConfigurationAttributeValueType valueType = grouperProvisioningConfigurationAttribute.getValueType();
    
    // only do this for strings
    if (valueType == null || valueType != GrouperProvisioningConfigurationAttributeValueType.STRING) {
      return;
    }

    // but not multivalued strings
    if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
      return;
    }
    
    // keep a count if supposed to
    if (count!= null) {
      count[0]++;
    }
    // assign empty
    provisioningUpdatable.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), "");
  }

  public void filterGroupFieldsAndAttributes(List<ProvisioningGroup> provisioningGroups, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    
    if (this.getGrouperProvisioner().retrieveGrouperTranslator().isTranslateGrouperToTargetAutomatically()) {
      return;
    }
    
    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> groupFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig();
    
    int[] filterGroupFieldsAndAttributesCount = new int[] {0};
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      
      provisioningGroup.setId((String)filterField(provisioningGroup.getId(), groupFieldNameToConfig.get("id"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));
      provisioningGroup.setDisplayName((String)filterField(provisioningGroup.getDisplayName(), groupFieldNameToConfig.get("displayName"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));
      provisioningGroup.setName((String)filterField(provisioningGroup.getName(), groupFieldNameToConfig.get("name"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));
      provisioningGroup.setIdIndex((Long)filterField(provisioningGroup.getIdIndex(), groupFieldNameToConfig.get("idIndex"),  filterSelect,  filterInsert,  filterUpdate, filterGroupFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values() ) {
        
        String attributeName = grouperProvisioningConfigurationAttribute.getName();
        //TODO maybe change this to not filter the membership attributes
        //If we are storing something to the grouper side for example in entity attribute the membership label
        // We are not selecting, inserting, or updating but we do want this attribute to exist
        if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect() && (grouperProvisioningConfigurationAttribute.isInsert() || grouperProvisioningConfigurationAttribute.isUpdate()) )
            || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
            || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
          provisioningGroup.removeAttribute(attributeName);
          filterGroupFieldsAndAttributesCount[0]++;
        }
      }
      
      GrouperUtil.nonNull(provisioningGroup.getAttributes()).keySet().retainAll(groupAttributeNameToConfig.keySet());

    }
    if (filterGroupFieldsAndAttributesCount[0] > 0) {
      filterGroupFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("filterGroupFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("filterGroupFieldsAndAttributesCount", filterGroupFieldsAndAttributesCount[0]);
      
    }
  }

  public void filterEntityFieldsAndAttributes(List<ProvisioningEntity> provisioningEntities, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    
    if (this.getGrouperProvisioner().retrieveGrouperTranslator().isTranslateGrouperToTargetAutomatically()) {
      return;
    }
    
    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> entityFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig();
    
    int[] filterEntityFieldsAndAttributesCount = new int[] {0};

    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      
      provisioningEntity.setId((String)filterField(provisioningEntity.getId(), entityFieldNameToConfig.get("id"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));
      provisioningEntity.setLoginId((String)filterField(provisioningEntity.getLoginId(), entityFieldNameToConfig.get("loginId"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));
      provisioningEntity.setName((String)filterField(provisioningEntity.getName(), entityFieldNameToConfig.get("name"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));
      provisioningEntity.setEmail((String)filterField(provisioningEntity.getEmail(), entityFieldNameToConfig.get("email"),  filterSelect,  filterInsert,  filterUpdate, filterEntityFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values() ) {
        
        String attributeName = grouperProvisioningConfigurationAttribute.getName();
        //TODO maybe change this to not filter the membership attributes
        //If we are storing something to the grouper side for example in entity attribute the membership label
        // We are not selecting, inserting, or updating but we do want this attribute to exist
        if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect() && (grouperProvisioningConfigurationAttribute.isInsert() || grouperProvisioningConfigurationAttribute.isUpdate()) )
            || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
            || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
          filterEntityFieldsAndAttributesCount[0]++;
          provisioningEntity.removeAttribute(attributeName);
        }
      }
      
      GrouperUtil.nonNull(provisioningEntity.getAttributes()).keySet().retainAll(entityAttributeNameToConfig.keySet());
      
    }
    if (filterEntityFieldsAndAttributesCount[0] > 0) {
      filterEntityFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("filterEntityFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("filterEntityFieldsAndAttributesCount", filterEntityFieldsAndAttributesCount[0]);
      
    }

  }

  public void filterMembershipFieldsAndAttributes(List<ProvisioningMembership> provisioningMemberships, boolean filterSelect, boolean filterInsert, boolean filterUpdate) {
    
    if (this.getGrouperProvisioner().retrieveGrouperTranslator().isTranslateGrouperToTargetAutomatically()) {
      return;
    }
    
    Map<String, GrouperProvisioningConfigurationAttribute> membershipAttributeNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> membershipFieldNameToConfig = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig();
    if (GrouperUtil.length(membershipAttributeNameToConfig) == 0 && GrouperUtil.length(membershipFieldNameToConfig) == 0) {
      return;
    }
    int[] filterMembershipFieldsAndAttributesCount = new int[] {0};

    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      
      provisioningMembership.setId((String)filterField(provisioningMembership.getId(), membershipFieldNameToConfig.get("id"),  filterSelect,  filterInsert,  filterUpdate, filterMembershipFieldsAndAttributesCount));
      provisioningMembership.setProvisioningEntityId((String)filterField(provisioningMembership.getProvisioningEntityId(), membershipFieldNameToConfig.get("provisioningEntityId"),  filterSelect,  filterInsert,  filterUpdate, filterMembershipFieldsAndAttributesCount));
      provisioningMembership.setProvisioningGroupId((String)filterField(provisioningMembership.getProvisioningGroupId(), membershipFieldNameToConfig.get("provisioningGroupId"),  filterSelect,  filterInsert,  filterUpdate, filterMembershipFieldsAndAttributesCount));

      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : membershipAttributeNameToConfig.values() ) {
        
        String attributeName = grouperProvisioningConfigurationAttribute.getName();
        if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect())
            || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
            || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
          filterMembershipFieldsAndAttributesCount[0]++;
          provisioningMembership.removeAttribute(attributeName);
        }
      }
      GrouperUtil.nonNull(provisioningMembership.getAttributes()).keySet().retainAll(membershipAttributeNameToConfig.keySet());

    }
    if (filterMembershipFieldsAndAttributesCount[0] > 0) {
      filterMembershipFieldsAndAttributesCount[0] += GrouperUtil.defaultIfNull((Integer)this.grouperProvisioner.getDebugMap().get("filterMembershipFieldsAndAttributesCount"), 0);
      this.grouperProvisioner.getDebugMap().put("filterMembershipFieldsAndAttributesCount", filterMembershipFieldsAndAttributesCount[0]);
      
    }

  }

  private Object filterField(Object value,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, boolean filterSelect, boolean filterInsert, boolean filterUpdate, int[] count) {
    if (value == null || grouperProvisioningConfigurationAttribute == null) {
      // if not configured to have this field, then dont
      return null;
    }
    if ((filterSelect && !grouperProvisioningConfigurationAttribute.isSelect())
        || (filterInsert && !grouperProvisioningConfigurationAttribute.isInsert())
        || (filterUpdate && !grouperProvisioningConfigurationAttribute.isUpdate())){
      count[0]++;
      return null;
    }
    return value;
      
  }



}
