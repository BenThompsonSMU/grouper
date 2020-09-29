package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * @author shilen
 */
public class GrouperProvisioningTranslatorBase {

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateGrouperToTarget() {
    this.translateGrouperToTarget(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects(), 
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects(), false);
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateGrouperToTargetIncludeDeletes() {
    // note, this wont do anything in a full sync since the includeDelete objects are not there
    this.translateGrouperToTarget(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjectsIncludeDeletes(), 
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes(), true);
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateGrouperToTarget(GrouperProvisioningLists grouperList, GrouperProvisioningLists targetList, boolean includeDelete) {
    

    {
      List<ProvisioningGroup> grouperProvisioningGroups = grouperList.getProvisioningGroups();
      List<ProvisioningGroup> grouperTargetGroups = translateGrouperToTargetGroups(grouperProvisioningGroups, includeDelete, false);
      targetList.setProvisioningGroups(grouperTargetGroups);
    }
    
    {
      List<ProvisioningEntity> grouperProvisioningEntities = grouperList.getProvisioningEntities();
      List<ProvisioningEntity> grouperTargetEntities = translateGrouperToTargetEntities(
          grouperProvisioningEntities, includeDelete, false);
      targetList.setProvisioningEntities(grouperTargetEntities);
    }    

    {
      List<ProvisioningMembership> grouperProvisioningMemberships = grouperList.getProvisioningMemberships();
      List<ProvisioningMembership> grouperTargetMemberships = translateGrouperToTargetMemberships(
          grouperProvisioningMemberships, includeDelete);
      targetList.setProvisioningMemberships(grouperTargetMemberships);
    }    
    
  }

  /**
   * keep a reference to the membership wrapper so attributes can register with membership
   */
  private static ThreadLocal<ProvisioningMembershipWrapper> provisioningMembershipWrapperThreadLocal = new InheritableThreadLocal<ProvisioningMembershipWrapper>();
  
  
  /**
   * keep a reference to the membership wrapper so attributes can register with membership
   * @return membership wrapper
   */
  public static ProvisioningMembershipWrapper retrieveProvisioningMembershipWrapper() {
    return provisioningMembershipWrapperThreadLocal.get();
  }

  public List<ProvisioningMembership> translateGrouperToTargetMemberships(
      List<ProvisioningMembership> grouperProvisioningMemberships, boolean includeDelete) {
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Membership"));

    for (ProvisioningMembership grouperProvisioningMembership: GrouperUtil.nonNull(grouperProvisioningMemberships)) {
      
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
 
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();

      ProvisioningMembership grouperTargetMembership = new ProvisioningMembership();
      grouperTargetMembership.setProvisioningMembershipWrapper(grouperProvisioningMembership.getProvisioningMembershipWrapper());
 
      provisioningMembershipWrapperThreadLocal.set(grouperProvisioningMembership.getProvisioningMembershipWrapper());
      try {

        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        
        elVariableMap.put("grouperProvisioningGroup", grouperProvisioningMembership.getProvisioningGroup());
        elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
        elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
        elVariableMap.put("gcGrouperSyncGroup", provisioningGroupWrapper.getGcGrouperSyncGroup());
 
        elVariableMap.put("grouperProvisioningEntity", grouperProvisioningMembership.getProvisioningEntity());
        elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
        elVariableMap.put("grouperTargetEntity", grouperTargetEntity);
        elVariableMap.put("gcGrouperSyncMember", provisioningEntityWrapper.getGcGrouperSyncMember());
        
        elVariableMap.put("grouperProvisioningMembership", grouperProvisioningMembership);
        elVariableMap.put("provisioningMembershipWrapper", grouperProvisioningMembership.getProvisioningMembershipWrapper());
        elVariableMap.put("grouperTargetMembership", grouperTargetMembership);
        elVariableMap.put("gcGrouperSyncMembership", grouperProvisioningMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership());

        // attribute translations
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeNameToConfig().values()) {
          String expression = grouperProvisioningConfigurationAttribute.getTranslateExpression();
          boolean hasExpression = !StringUtils.isBlank(expression);
          String expressionToUse = null;
          if (hasExpression) {
            expressionToUse = expression;
          }
          if (!StringUtils.isBlank(expressionToUse)) {
            Object result = runScript(expressionToUse, elVariableMap);
            result = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(result, grouperProvisioningConfigurationAttribute);
            grouperTargetGroup.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
          }
        }
        
        // field configurations
        grouperTargetMembership.setId(GrouperUtil.stringValue(fieldTranslation( 
            grouperTargetMembership.getId(), elVariableMap, false, 
            this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("id"))));
        grouperTargetMembership.setProvisioningEntityId(GrouperUtil.stringValue(fieldTranslation( 
            grouperTargetMembership.getProvisioningEntityId(), elVariableMap, false, 
            this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("provisioningEntityId"))));
        grouperTargetMembership.setProvisioningGroupId(GrouperUtil.stringValue(fieldTranslation( 
            grouperTargetMembership.getProvisioningGroupId(), elVariableMap, false, 
            this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("provisioningGroupId"))));

        for (String script: scripts) {

          runScript(script, elVariableMap);
          
        }
      } finally {
        provisioningMembershipWrapperThreadLocal.remove();
      }
      if (grouperTargetMembership.isRemoveFromList() || grouperTargetMembership.isEmpty()) {
        continue;
      }
      if (grouperTargetGroup != null) {
        if (!StringUtils.equals(grouperTargetGroup.getId(), grouperTargetMembership.getProvisioningGroupId())) {
          grouperTargetMembership.setProvisioningGroupId(grouperTargetGroup.getId());
          grouperTargetMembership.setProvisioningGroup(grouperTargetGroup);
        }
      }
      
      if (grouperTargetEntity != null) {
        if (!StringUtils.equals(grouperTargetEntity.getId(), grouperTargetMembership.getProvisioningEntityId())) {
          grouperTargetMembership.setProvisioningEntityId(grouperTargetEntity.getId());
          grouperTargetMembership.setProvisioningEntity(grouperTargetEntity);
        }
      }

      if (includeDelete) {
        grouperTargetMembership.getProvisioningMembershipWrapper().setGrouperTargetMembershipIncludeDelete(grouperTargetMembership);
      } else {
        grouperTargetMembership.getProvisioningMembershipWrapper().setGrouperTargetMembership(grouperTargetMembership);
      }

      grouperTargetMemberships.add(grouperTargetMembership); 
    }
    return grouperTargetMemberships;
  }

  public List<ProvisioningEntity> translateGrouperToTargetEntities(
      List<ProvisioningEntity> grouperProvisioningEntities, boolean includeDelete, boolean forCreate) {
    
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Entity"));
    
    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("EntityCreateOnly")));
    }

    if (GrouperUtil.length(scripts) == 0) {
      return grouperTargetEntities;
    }
 
    for (ProvisioningEntity grouperProvisioningEntity: GrouperUtil.nonNull(grouperProvisioningEntities)) {
      
      ProvisioningEntity grouperTargetEntity = new ProvisioningEntity();
      grouperTargetEntity.setProvisioningEntityWrapper(grouperProvisioningEntity.getProvisioningEntityWrapper());

      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
      elVariableMap.put("provisioningEntityWrapper", grouperProvisioningEntity.getProvisioningEntityWrapper());
      elVariableMap.put("gcGrouperSyncMember", grouperProvisioningEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember());
      elVariableMap.put("grouperTargetEntity", grouperTargetEntity);

      // attribute translations
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeNameToConfig().values()) {
        String expression = grouperProvisioningConfigurationAttribute.getTranslateExpression();
        boolean hasExpression = !StringUtils.isBlank(expression);
        String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateExpressionCreateOnly();
        boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
        String expressionToUse = null;
        if (forCreate && hasExpressionCreateOnly) {
          expressionToUse = expressionCreateOnly;
        } else if (hasExpression) {
          expressionToUse = expression;
        }
        if (!StringUtils.isBlank(expressionToUse)) {
          Object result = runScript(expressionToUse, elVariableMap);
          result = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(result, grouperProvisioningConfigurationAttribute);
          grouperTargetEntity.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
        }
      }
      
      // field configurations
      grouperTargetEntity.setId(GrouperUtil.stringValue(fieldTranslation( 
          grouperTargetEntity.getId(), elVariableMap, forCreate, 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("id"))));
      grouperTargetEntity.setName(GrouperUtil.stringValue(fieldTranslation( 
          grouperTargetEntity.getName(), elVariableMap, forCreate, 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("name"))));
      grouperTargetEntity.setEmail(GrouperUtil.stringValue(fieldTranslation( 
          grouperTargetEntity.getEmail(), elVariableMap, forCreate, 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("email"))));
      grouperTargetEntity.setLoginId(GrouperUtil.stringValue(fieldTranslation( 
          grouperTargetEntity.getLoginId(), elVariableMap, forCreate, 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("loginId"))));

      for (String script: scripts) {
               
        runScript(script, elVariableMap);
        
      }

      if (grouperTargetEntity.isRemoveFromList() || grouperTargetEntity.isEmpty()) {
        continue;
      }
      
      grouperTargetEntities.add(grouperTargetEntity);
      
      if (includeDelete) {
        grouperProvisioningEntity.getProvisioningEntityWrapper().setGrouperTargetEntityIncludeDelete(grouperTargetEntity);
      } else if (forCreate) {
        grouperProvisioningEntity.getProvisioningEntityWrapper().setGrouperTargetEntityForCreate(grouperTargetEntity);
      } else {
        grouperProvisioningEntity.getProvisioningEntityWrapper().setGrouperTargetEntity(grouperTargetEntity);
      }

    }
    return grouperTargetEntities;
  }

  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups, boolean includeDelete, boolean forCreate) {

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Group"));

    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("GroupCreateOnly")));
    }
    List<ProvisioningGroup> grouperTargetGroups = new ArrayList<ProvisioningGroup>();

    if (GrouperUtil.length(scripts) == 0) {
      return grouperTargetGroups;
    }
    
    for (ProvisioningGroup grouperProvisioningGroup: GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      ProvisioningGroup grouperTargetGroup = new ProvisioningGroup();
      grouperTargetGroup.setProvisioningGroupWrapper(grouperProvisioningGroup.getProvisioningGroupWrapper());

      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
      elVariableMap.put("provisioningGroupWrapper", grouperProvisioningGroup.getProvisioningGroupWrapper());
      elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
      elVariableMap.put("gcGrouperSyncGroup", grouperProvisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup());

      // attribute translations
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeNameToConfig().values()) {
        String expression = grouperProvisioningConfigurationAttribute.getTranslateExpression();
        boolean hasExpression = !StringUtils.isBlank(expression);
        String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateExpressionCreateOnly();
        boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
        String expressionToUse = null;
        if (forCreate && hasExpressionCreateOnly) {
          expressionToUse = expressionCreateOnly;
        } else if (hasExpression) {
          expressionToUse = expression;
        }
        if (!StringUtils.isBlank(expressionToUse)) {
          Object result = runScript(expressionToUse, elVariableMap);
          result = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(result, grouperProvisioningConfigurationAttribute);
          grouperTargetGroup.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
        }
      }
      
      // field configurations
      grouperTargetGroup.setId(GrouperUtil.stringValue(fieldTranslation( 
          grouperTargetGroup.getId(), elVariableMap, forCreate, 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("id"))));
      grouperTargetGroup.setName(GrouperUtil.stringValue(fieldTranslation( 
          grouperTargetGroup.getName(), elVariableMap, forCreate, 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("name"))));
      grouperTargetGroup.setIdIndex(GrouperUtil.longObjectValue(fieldTranslation( 
          grouperTargetGroup.getIdIndex(), elVariableMap, forCreate, 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("idIndex")), true));
      grouperTargetGroup.setDisplayName(GrouperUtil.stringValue(fieldTranslation( 
          grouperTargetGroup.getDisplayName(), elVariableMap, forCreate, 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupFieldNameToConfig().get("displayName"))));
      
      
      for (String script: scripts) {

        
        runScript(script, elVariableMap);
        
      }

      if (grouperTargetGroup.isRemoveFromList() || grouperTargetGroup.isEmpty()) {
        continue;
      }

      if (includeDelete) {
        grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperTargetGroupIncludeDelete(grouperTargetGroup);
      } else if (forCreate) {
        grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperTargetGroupForCreate(grouperTargetGroup);
      } else {
        grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperTargetGroup(grouperTargetGroup);
      }
      grouperTargetGroups.add(grouperTargetGroup);
        
    }
    return grouperTargetGroups;
  }

  public Object fieldTranslation(Object currentValue, Map<String, Object> elVariableMap, boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    if (grouperProvisioningConfigurationAttribute == null) {
      return currentValue;
    }
    String expression = grouperProvisioningConfigurationAttribute.getTranslateExpression();
    boolean hasExpression = !StringUtils.isBlank(expression);
    String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateExpressionCreateOnly();
    boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
    String expressionToUse = null;
    if (forCreate && hasExpressionCreateOnly) {
      expressionToUse = expressionCreateOnly;
    } else if (hasExpression) {
      expressionToUse = expression;
    }
    if (!StringUtils.isBlank(expressionToUse)) {
      Object result = runScript(expressionToUse, elVariableMap);
      return result;
    }
    return currentValue;
  }

  public void idTargetGroups(List<ProvisioningGroup> targetGroups) {

    if (GrouperUtil.isBlank(targetGroups)) {
      return;
    }

    String groupIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupTargetIdExpression(); 

    String groupIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupTargetIdAttribute();

    String groupIdField = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupTargetIdField();

    if (StringUtils.isBlank(groupIdScript) && StringUtils.isBlank(groupIdAttribute) && StringUtils.isBlank(groupIdField)) {
      return;
    }
    
    for (ProvisioningGroup targetGroup: GrouperUtil.nonNull(targetGroups)) {
      
      Object id = null;
      if (!StringUtils.isBlank(groupIdField)) {
        if ("id".equals(groupIdField)) {
          id = targetGroup.getId();
        } else if ("idIndex".equals(groupIdField)) {
          id = targetGroup.getIdIndex();
        } else if ("name".equals(groupIdField)) {
          id = targetGroup.getName();
        } else {
          throw new RuntimeException("Invalid groupTargetIdField, expecting id, idIndex, or name");
        }
        
      } else if (!StringUtils.isBlank(groupIdAttribute)) {
        Object idValue = targetGroup.retrieveAttributeValue(groupIdAttribute);
        if (idValue instanceof Collection) {
          throw new RuntimeException("Cant have a multivalued target id attribute: '" + groupIdAttribute + "', " + targetGroup);
        }
        id = idValue;

      } else if (!StringUtils.isBlank(groupIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetGroup", targetGroup);
        
        id = runScript(groupIdScript, elVariableMap);

      } else {
        throw new RuntimeException("Must have groupTargetIdField, groupTargetIdAttribute, or groupTargetIdExpression");
      }
      id = massageToString(id, 2);
      targetGroup.setTargetId(id);

    }
  }

  public void idTargetEntities(List<ProvisioningEntity> targetEntities) {

    if (GrouperUtil.isBlank(targetEntities)) {
      return;
    }

    String entityIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityTargetIdExpression(); 

    String entityIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityTargetIdAttribute();

    String entityIdField = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityTargetIdField();

    if (StringUtils.isBlank(entityIdScript) && StringUtils.isBlank(entityIdAttribute) && StringUtils.isBlank(entityIdField)) {
      return;
    }
    
    for (ProvisioningEntity targetEntity: GrouperUtil.nonNull(targetEntities)) {
      
      Object id = null;
      if (!StringUtils.isBlank(entityIdField)) {
        if ("id".equals(entityIdField)) {
          id = targetEntity.getId();
        } else if ("subjectId".equals(entityIdField)) {
          id = targetEntity.getSubjectId();
        } else if ("loginId".equals(entityIdField)) {
          id = targetEntity.getLoginId();
        } else {
          throw new RuntimeException("Invalid entityTargetIdField, expecting id, subjectId, or loginId");
        }
      } else if (!StringUtils.isBlank(entityIdAttribute)) {
        Object idValue = targetEntity.retrieveAttributeValue(entityIdAttribute);
        if (idValue instanceof Collection) {
          throw new RuntimeException("Cant have a multivalued target id attribute: '" + entityIdAttribute + "', " + targetEntity);
        }
        id = idValue;
      } else if (!StringUtils.isBlank(entityIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetEntity", targetEntity);
        
        id = runScript(entityIdScript, elVariableMap);
                
      } else {
        throw new RuntimeException("Must have entityTargetIdField, entityTargetIdAttribute, or entityTargetIdExpression");
      }

      id = massageToString(id, 2);
      
      targetEntity.setTargetId(id);

      
    }
  }

  public Object massageToString(Object id, int timeToLive) {
    if (timeToLive-- < 0) {
      throw new RuntimeException("timeToLive expired?????  why????");
    }
    if (id == null) {
      return null;
    }
    if (id instanceof String) {
      return id;
    }
    if (id instanceof Number) {
      return id.toString();
    }
    if (id instanceof MultiKey) {
      MultiKey idMultiKey = (MultiKey)id;
      Object[] newMultiKey = new Object[idMultiKey.size()];
      for (int i=0;i<newMultiKey.length;i++) {
        newMultiKey[i] = massageToString(idMultiKey.getKey(i), timeToLive);
      }
      return new MultiKey(newMultiKey);
    }
    // uh...
    throw new RuntimeException("target ids should be string, number, or multikey of string and number! " + id.getClass() + ", " + id);
  }

  public void idTargetMemberships(List<ProvisioningMembership> targetMemberships) {

    if (GrouperUtil.isBlank(targetMemberships)) {
      return;
    }
    String membershipIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipTargetIdExpression(); 

    String membershipIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipTargetIdAttribute();

    String membershipIdField = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipTargetIdField();

    if (StringUtils.isBlank(membershipIdScript) && StringUtils.isBlank(membershipIdAttribute) && StringUtils.isBlank(membershipIdField)) {
      return;
    }
    
    for (ProvisioningMembership targetMembership: GrouperUtil.nonNull(targetMemberships)) {
      
      Object id = null;
      if (!StringUtils.isBlank(membershipIdField)) {
        if ("id".equals(membershipIdField)) {
          id = targetMembership.getId();
        } else if ("provisioningGroupId,provisioningMembershipId".equals(membershipIdField)) {
          id = new MultiKey(targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
        } else {
          throw new RuntimeException("Invalid membershipTargetIdField, expecting id or 'provisioningGroupId,provisioningMembershipId'");
        }
      } else if (!StringUtils.isBlank(membershipIdAttribute)) {
        Object idValue = targetMembership.retrieveAttributeValue(membershipIdAttribute);
        if (idValue instanceof Collection) {
          throw new RuntimeException("Cant have a multivalued target id attribute: '" + membershipIdAttribute + "', " + targetMembership);
        }
        id = idValue;
      } else if (!StringUtils.isBlank(membershipIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetMembership", targetMembership);
        
        id = runScript(membershipIdScript, elVariableMap);

                
      } else {
        throw new RuntimeException("Must have membershipTargetIdField, membershipTargetIdAttribute, or membershipTargetIdExpression");
      }
      id = massageToString(id, 2);
      targetMembership.setTargetId(id);

    }

  }

  public Object runScript(String script, Map<String, Object> elVariableMap) {
    try {
      if (!script.contains("${")) {
        script = "${" + script + "}";
      }
      return GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, false);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }

  public Object runExpression(String script, Map<String, Object> elVariableMap) {
    try {
      if (!script.contains("${")) {
        script = "${" + script + "}";
      }
      return GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }

  public void targetIdTargetObjects() {
    idTargetGroups(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningMemberships());
  }
  
  public void targetIdGrouperObjects() {
    idTargetGroups(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships());

  }
  public void targetIdGrouperObjectsIncludeDeletes() {
    idTargetGroups(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningMemberships());

  }

}