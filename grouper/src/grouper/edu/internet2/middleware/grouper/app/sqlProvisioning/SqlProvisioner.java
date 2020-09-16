package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> retrieveTargetDaoClass() {
    if (!this.retrieveProvisioningConfiguration().isConfigured()) {
      throw new RuntimeException("Why is provisioner not configured???");
    }
    return this.retrieveSqlProvisioningConfiguration().getSqlProvisioningType().sqlTargetDaoClass();
  }

  public SqlProvisioningConfiguration retrieveSqlProvisioningConfiguration() {
    return (SqlProvisioningConfiguration)this.retrieveProvisioningConfiguration();
  }
  
  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> retrieveProvisioningConfigurationClass() {
    return SqlProvisioningConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(
      GrouperProvisioningBehavior grouperProvisioningBehavior) {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = this.retrieveSqlProvisioningConfiguration();
    switch (sqlProvisioningConfiguration.getSqlProvisioningType()) {
      case sqlLikeLdapGroupMemberships:
        grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.groupAttributes);
        break;
      default:
        throw new RuntimeException("Not expecting type: " + sqlProvisioningConfiguration.getSqlProvisioningType());
    }
  }

}
