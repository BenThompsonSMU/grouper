package edu.internet2.middleware.grouper.app.provisioning;


public class ProvisioningObjectChange {
  
  public ProvisioningObjectChange() {
    super();
  }

  
  public ProvisioningObjectChange(
      ProvisioningObjectChangeDataType provisioningObjectChangeDataType, String fieldName,
      String attributeName, ProvisioningObjectChangeAction provisioningObjectChangeAction,
      Object oldValue, Object newValue) {
    super();
    this.provisioningObjectChangeDataType = provisioningObjectChangeDataType;
    this.fieldName = fieldName;
    this.attributeName = attributeName;
    this.provisioningObjectChangeAction = provisioningObjectChangeAction;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }


  /**
   * field or attribute
   */
  private ProvisioningObjectChangeDataType provisioningObjectChangeDataType;

  public ProvisioningObjectChangeDataType getProvisioningObjectChangeDataType() {
    return provisioningObjectChangeDataType;
  }
  
  public void setProvisioningObjectChangeDataType(
      ProvisioningObjectChangeDataType provisioningObjectChangeDataType) {
    this.provisioningObjectChangeDataType = provisioningObjectChangeDataType;
  }
  
  /**
   * if field this is the field name
   */
  private String fieldName;
  
  /**
   * if attribute this is the attribute name
   */
  private String attributeName;
  
  /**
   * if this is an insert, update, or delete
   */
  private ProvisioningObjectChangeAction provisioningObjectChangeAction;
  
  /**
   * previous value if not an insert
   */
  private Object oldValue;
  
  /**
   * new value if not a delete
   */
  private Object newValue;

  
  public String getFieldName() {
    return fieldName;
  }

  
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  
  public String getAttributeName() {
    return attributeName;
  }

  
  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  
  public ProvisioningObjectChangeAction getProvisioningObjectChangeAction() {
    return provisioningObjectChangeAction;
  }

  
  public void setProvisioningObjectChangeAction(
      ProvisioningObjectChangeAction provisioningObjectChangeAction) {
    this.provisioningObjectChangeAction = provisioningObjectChangeAction;
  }

  
  public Object getOldValue() {
    return oldValue;
  }

  
  public void setOldValue(Object oldValue) {
    this.oldValue = oldValue;
  }

  
  public Object getNewValue() {
    return newValue;
  }

  
  public void setNewValue(Object newValue) {
    this.newValue = newValue;
  }

  
  
}
