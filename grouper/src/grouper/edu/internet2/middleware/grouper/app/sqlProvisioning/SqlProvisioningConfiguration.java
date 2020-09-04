package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;


public class SqlProvisioningConfiguration extends GrouperProvisioningConfigurationBase {

  private String groupAttributeTableAttributeNameIsGroupId;
  
  
  public String getGroupAttributeTableAttributeNameIsGroupId() {
    return groupAttributeTableAttributeNameIsGroupId;
  }



  
  public void setGroupAttributeTableAttributeNameIsGroupId(
      String groupAttributeTableAttributeNameIsGroupId) {
    this.groupAttributeTableAttributeNameIsGroupId = groupAttributeTableAttributeNameIsGroupId;
  }



  private String dbExternalSystemConfigId;
  
  private String membershipTableName;

  private String membershipUserColumn;
  
  private String membershipUserValueFormat;
  
  private String membershipGroupColumn;
  
  private String membershipGroupValueFormat;
  
  private String membershipCreationNumberOfAttributes;
  
  private String membershipCreationColumnTemplate_attr_0;
  
  private String membershipCreationColumnTemplate_val_0;
  
  private String membershipCreationColumnTemplate_attr_1;
  
  private String membershipCreationColumnTemplate_val_1;
  
  /** 
   * table name for group table
   */
  private String groupTableName;
  
  /**
   * columns in the group table
   */
  private String groupAttributeNames;
  
  /**
   * if there is a group attribute table (like ldap), this is the table name
   */
  private String groupAttributeTableName;
  
  /**
   * if group table has one primary key, this is it
   */
  private String groupTableIdColumn;
  
  /**
   * single column in group attribute table that links back to group pk
   */
  private String groupAttributeTableForeignKeyToGroup;
  
  /**
   * primary key column of attribute table
   */
  private String groupAttributeTableIdColumn;
  
  /**
   * group attribute table has a column which is the attribute name
   */
  private String groupAttributeTableAttributeNameColumn;
  
  /**
   * group attribute table has attribute value column
   */
  private String groupAttributeTableAttributeValueColumn;
  
  /**
   * table name for group table
   * @return
   */
  public String getGroupTableName() {
    return groupTableName;
  }



  /**
   * table name for group table
   * @param groupTableName
   */
  public void setGroupTableName(String groupTableName) {
    this.groupTableName = groupTableName;
  }



  
  public String getGroupAttributeNames() {
    return groupAttributeNames;
  }



  
  public void setGroupAttributeNames(String groupAttributeNames) {
    this.groupAttributeNames = groupAttributeNames;
  }



  
  public String getGroupAttributeTableName() {
    return groupAttributeTableName;
  }



  
  public void setGroupAttributeTableName(String groupAttributeTableName) {
    this.groupAttributeTableName = groupAttributeTableName;
  }



  
  public String getGroupTableIdColumn() {
    return groupTableIdColumn;
  }



  
  public void setGroupTableIdColumn(String groupTableIdColumn) {
    this.groupTableIdColumn = groupTableIdColumn;
  }



  
  public String getGroupAttributeTableForeignKeyToGroup() {
    return groupAttributeTableForeignKeyToGroup;
  }



  
  public void setGroupAttributeTableForeignKeyToGroup(
      String groupAttributeTableForeignKeyToGroup) {
    this.groupAttributeTableForeignKeyToGroup = groupAttributeTableForeignKeyToGroup;
  }



  
  public String getGroupAttributeTableIdColumn() {
    return groupAttributeTableIdColumn;
  }



  
  public void setGroupAttributeTableIdColumn(String groupAttributeTableIdColumn) {
    this.groupAttributeTableIdColumn = groupAttributeTableIdColumn;
  }



  
  public String getGroupAttributeTableAttributeNameColumn() {
    return groupAttributeTableAttributeNameColumn;
  }



  
  public void setGroupAttributeTableAttributeNameColumn(
      String groupAttributeTableAttributeNameColumn) {
    this.groupAttributeTableAttributeNameColumn = groupAttributeTableAttributeNameColumn;
  }



  
  public String getGroupAttributeTableAttributeValueColumn() {
    return groupAttributeTableAttributeValueColumn;
  }



  
  public void setGroupAttributeTableAttributeValueColumn(
      String groupAttributeTableAttributeValueColumn) {
    this.groupAttributeTableAttributeValueColumn = groupAttributeTableAttributeValueColumn;
  }



  @Override
  public void configureSpecificSettings() {
    
    this.dbExternalSystemConfigId = this.retrieveConfigString("dbExternalSystemConfigId", true);
    
    //TODO validate sql config id
    
    this.membershipTableName = this.retrieveConfigString("membershipTableName", false);
    
//    this.membershipUserColumn = this.retrieveConfigString("membershipUserColumn", false);
//    this.membershipUserValueFormat = this.retrieveConfigString("membershipUserValueFormat", true);
//    this.membershipGroupColumn = this.retrieveConfigString("membershipGroupColumn", true);
//    this.membershipGroupValueFormat = this.retrieveConfigString("membershipGroupValueFormat", true);
//    this.membershipCreationNumberOfAttributes = this.retrieveConfigString("membershipCreationNumberOfAttributes", true);
//    this.membershipCreationColumnTemplate_attr_0 = this.retrieveConfigString("membershipCreationColumnTemplate_attr_0", true);
//    this.membershipCreationColumnTemplate_val_0 = this.retrieveConfigString("membershipCreationColumnTemplate_val_0", true);
//    this.membershipCreationColumnTemplate_attr_1 = this.retrieveConfigString("membershipCreationColumnTemplate_attr_1", true);
//    this.membershipCreationColumnTemplate_val_1 = this.retrieveConfigString("membershipCreationColumnTemplate_val_1", true);

    
    
    this.groupAttributeTableAttributeNameIsGroupId = this.retrieveConfigString("groupAttributeTableAttributeNameIsGroupId", false);
    this.groupTableName = this.retrieveConfigString("groupTableName", false);
    this.groupAttributeNames = this.retrieveConfigString("groupAttributeNames", false);
    this.groupAttributeTableName = this.retrieveConfigString("groupAttributeTableName", false);
    this.groupTableIdColumn = this.retrieveConfigString("groupTableIdColumn", false);
    this.groupAttributeTableForeignKeyToGroup = this.retrieveConfigString("groupAttributeTableForeignKeyToGroup", false);
    this.groupAttributeTableIdColumn = this.retrieveConfigString("groupAttributeTableIdColumn", false);
    this.groupAttributeTableAttributeNameColumn = this.retrieveConfigString("groupAttributeTableAttributeNameColumn", false);
    this.groupAttributeTableAttributeValueColumn = this.retrieveConfigString("groupAttributeTableAttributeValueColumn", false);

  }


  
  public String getDbExternalSystemConfigId() {
    return dbExternalSystemConfigId;
  }


  
  public void setDbExternalSystemConfigId(String dbExternalSystemConfigId) {
    this.dbExternalSystemConfigId = dbExternalSystemConfigId;
  }


  
  public String getMembershipTableName() {
    return membershipTableName;
  }


  
  public void setMembershipTableName(String membershipTableName) {
    this.membershipTableName = membershipTableName;
  }


  
  public String getMembershipUserColumn() {
    return membershipUserColumn;
  }


  
  public void setMembershipUserColumn(String membershipUserColumn) {
    this.membershipUserColumn = membershipUserColumn;
  }


  
  public String getMembershipUserValueFormat() {
    return membershipUserValueFormat;
  }


  
  public void setMembershipUserValueFormat(String membershipUserValueFormat) {
    this.membershipUserValueFormat = membershipUserValueFormat;
  }


  
  public String getMembershipGroupColumn() {
    return membershipGroupColumn;
  }


  
  public void setMembershipGroupColumn(String membershipGroupColumn) {
    this.membershipGroupColumn = membershipGroupColumn;
  }


  
  public String getMembershipGroupValueFormat() {
    return membershipGroupValueFormat;
  }


  
  public void setMembershipGroupValueFormat(String membershipGroupValueFormat) {
    this.membershipGroupValueFormat = membershipGroupValueFormat;
  }


  
  public String getMembershipCreationNumberOfAttributes() {
    return membershipCreationNumberOfAttributes;
  }


  
  public void setMembershipCreationNumberOfAttributes(
      String membershipCreationNumberOfAttributes) {
    this.membershipCreationNumberOfAttributes = membershipCreationNumberOfAttributes;
  }


  
  public String getMembershipCreationColumnTemplate_attr_0() {
    return membershipCreationColumnTemplate_attr_0;
  }


  
  public void setMembershipCreationColumnTemplate_attr_0(
      String membershipCreationColumnTemplate_attr_0) {
    this.membershipCreationColumnTemplate_attr_0 = membershipCreationColumnTemplate_attr_0;
  }


  
  public String getMembershipCreationColumnTemplate_val_0() {
    return membershipCreationColumnTemplate_val_0;
  }


  
  public void setMembershipCreationColumnTemplate_val_0(
      String membershipCreationColumnTemplate_val_0) {
    this.membershipCreationColumnTemplate_val_0 = membershipCreationColumnTemplate_val_0;
  }


  
  public String getMembershipCreationColumnTemplate_attr_1() {
    return membershipCreationColumnTemplate_attr_1;
  }


  
  public void setMembershipCreationColumnTemplate_attr_1(
      String membershipCreationColumnTemplate_attr_1) {
    this.membershipCreationColumnTemplate_attr_1 = membershipCreationColumnTemplate_attr_1;
  }


  
  public String getMembershipCreationColumnTemplate_val_1() {
    return membershipCreationColumnTemplate_val_1;
  }


  
  public void setMembershipCreationColumnTemplate_val_1(
      String membershipCreationColumnTemplate_val_1) {
    this.membershipCreationColumnTemplate_val_1 = membershipCreationColumnTemplate_val_1;
  }

}
