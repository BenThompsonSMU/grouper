package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoSendChangesToTargetRequest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * does the logic to use the data from the DAOs and call the correct methods to synnc things up or dry run or send messages for async
 * @author mchyzer
 *
 */
public class GrouperProvisioningLogic {
  
  /**
   * 
   */
  public void provision() {

    // let the target dao tell the framework what it can do
    this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getWrappedDao().registerGrouperProvisionerDaoCapabilities(
        this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities()
        );

    // let the provisioner tell the framework how the provisioner should behave with respect to the target
    this.getGrouperProvisioner().registerProvisioningBehaviors(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior());
    
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.configure);

    this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().provision(this.grouperProvisioner);
    
  }

  /**
   * 
   */
  public void provisionFull() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    try {
      debugMap.put("state", "retrieveAllDataFromGrouperAndTarget");
      long start = System.currentTimeMillis();
      grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveAllData();
      long retrieveDataPass1 = System.currentTimeMillis()-start;
      debugMap.put("retrieveDataPass1_millis", retrieveDataPass1);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveAllDataFromGrouperAndTarget);
    }

    try {
      debugMap.put("state", "targetAttributeManipulation");
      // assign matching id to target objects
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterForSelect(
          this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects());
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributes(
          this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects());
      
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetAttributeManipulation);
    }
    

    try {
      debugMap.put("state", "matchingIdTargetObjects");
      // assign matching id to target objects
      this.grouperProvisioner.retrieveGrouperTranslator().matchingIdTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdTargetObjects);
    }
    
    try {
      debugMap.put("state", "retrieveSubjectLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().retrieveSubjectLink();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveSubjectLink);
    }

    try {
  
      debugMap.put("state", "translateGrouperGroupsEntitiesToTarget");

      {
        List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups();
        List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningGroups(grouperTargetGroups);
      }
      
      {
        List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningEntities();
        List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(
            grouperProvisioningEntities, false, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningEntities(grouperTargetEntities);
      }    

    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperGroupsEntitiesToTarget);
    }

    try {
      debugMap.put("state", "matchingIdGrouperGroupsEntities");
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().getProvisioningGroups());
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().getProvisioningEntities());
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperGroupsEntities);
    }

    {
      debugMap.put("state", "indexMatchingIdOfGrouperGroups");
      
      // index the groups and entity matching ids
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdOfGrouperGroups(
          this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().getProvisioningGroups());
      
      debugMap.put("state", "indexMatchingIdOfGrouperEntities");
      
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdOfGrouperEntities(
          this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().getProvisioningEntities());
    }

    debugMap.put("state", "indexMatchingIdOfTargetGroupsEntities");
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdOfTargetGroups();
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdOfTargetEntities();

    debugMap.put("state", "createMissingGroups");
    createMissingGroupsFull();

    debugMap.put("state", "createMissingEntities");
    createMissingEntitiesFull();

    try {
      debugMap.put("state", "retrieveTargetGroupLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateGroupLinkFull();
      
      debugMap.put("state", "retrieveTargetEntityLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLinkFull();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.linkData);
    }
      
    try {
  
      debugMap.put("state", "translateGrouperMembershipsToTarget");

      {
        List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningMemberships();
        List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetMemberships(
            grouperProvisioningMemberships, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningMemberships(grouperTargetMemberships);
      }    

    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget);
    }

    try {
      debugMap.put("state", "matchingIdGrouperMemberships");
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().getProvisioningMemberships());
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
    }

    // index the memberships
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdOfGrouperMemberships(
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().getProvisioningMemberships());

    // index target memberships
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdOfTargetMemberships();

    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectGroupsFull(this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper().values());
    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectEntitiesFull(this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper().values());
    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectMembershipsFull(this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMembershipMatchingIdToProvisioningMembershipWrapper().values());
    
    try {
      debugMap.put("state", "compareTargetObjects");
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
    }
    
    this.countInsertsUpdatesDeletes();

    RuntimeException runtimeException = null;
    try {
      debugMap.put("state", "sendChangesToTarget");
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
      targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
      targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
      targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
    } catch (RuntimeException e) {
      runtimeException = e;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsUpdatesFull(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }
      //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.sendChangesToTarget);

    }
  
    {
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
      gcGrouperSync.setLastFullSyncRun(nowTimestamp);

      GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
      gcGrouperSyncJob.setErrorMessage(null);
      gcGrouperSyncJob.setErrorTimestamp(null);
      gcGrouperSyncJob.setLastSyncTimestamp(nowTimestamp);
      if (this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().wasWorkDone()) {
        gcGrouperSyncJob.setLastTimeWorkWasDone(nowTimestamp);
      }
      gcGrouperSyncJob.setPercentComplete(100);
      
      // do this in the right spot, after assigning correct sync info about sync
      int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
  
      this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
    }
    

    // TODO flesh this out, resolve subjects, linked cached data, etc, try individually again
//    this.getGrouperProvisioner().retrieveTargetDao().resolveErrors();
//    this.getGrouperProvisioner().retrieveTargetDao().sendErrorFixesToTarget();

//    this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedGroupsToTarget();
//    this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedMembershipsToTarget();

    // make sure the sync objects are correct
//    new ProvisioningSyncIntegration().assignTarget(this.getGrouperProvisioner().getConfigId()).fullSync();

//    // step 1
//    debugMap.put("state", "retrieveData");
//    this.gcTableSyncConfiguration.getGcTableSyncSubtype().retrieveData(debugMap, this);
//    
//    this.gcGrouperSyncLog.setRecordsProcessed(Math.max(this.gcTableSyncOutput.getRowsSelectedFrom(), this.gcTableSyncOutput.getRowsSelectedTo()));
//
//    if (this.gcGrouperSyncHeartbeat.isInterrupted()) {
//      debugMap.put("interrupted", true);
//      debugMap.put("state", "done");
//      gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.INTERRUPTED);
//      return;
//    }
    
  }

  /**
   * 
   */
  public void provisionIncremental() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    try {
      debugMap.put("state", "retrieveIncrementalDataFromGrouper");
      long start = System.currentTimeMillis();
      grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveGrouperDataIncremental();
      long retrieveDataPass1 = System.currentTimeMillis()-start;
      debugMap.put("retrieveDataPass1_millis", retrieveDataPass1);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIncrementalDataFromGrouper);
    }

    try {
      debugMap.put("state", "matchingIdTargetObjects");
      this.grouperProvisioner.retrieveGrouperTranslator().matchingIdTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdTargetObjects);
    }

    try {
      debugMap.put("state", "retrieveSubjectLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().retrieveSubjectLink();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveSubjectLink);
    }

    debugMap.put("state", "retrieveMissingObjects");
    retrieveMissingObjectsIncremental();

    debugMap.put("state", "createMissingObjects");
    //TODO createMissingGroupsPass1();
    //TODO createMissingEntitiesPass1();

    try {
      debugMap.put("state", "retrieveTargetGroupLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateGroupLinkIncremental();
      
      debugMap.put("state", "retrieveTargetEntityLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLinkIncremental();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.linkData);
    }
      
    try {
  
      debugMap.put("state", "translateGrouperToTarget");
      this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTarget();
      // note in a full sync this wont do anything since there are no includeDelete objects there
      this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetIncludeDeletes();
    } finally {
      //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperToTarget);
    }

    try {
      debugMap.put("state", "matchingIdGrouperObjects");
      this.grouperProvisioner.retrieveGrouperTranslator().matchingIdGrouperObjects();
      this.grouperProvisioner.retrieveGrouperTranslator().matchingIdGrouperObjectsIncludeDeletes();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperObjects);
    }

    debugMap.put("state", "indexMatchingIdOfGrouperObjects");
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdOfGrouperObjects();

    try {
      debugMap.put("state", "retrieveDataPass2");
      long start = System.currentTimeMillis();
      
      grouperProvisioner.retrieveGrouperProvisioningLogic().setupIncrementalGrouperTargetObjectsToRetrieveFromTarget();
      TargetDaoRetrieveIncrementalDataResponse targetDaoRetrieveIncrementalDataResponse 
        = grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveIncrementalData(grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest());
      if (targetDaoRetrieveIncrementalDataResponse != null) {
        grouperProvisioner.retrieveGrouperProvisioningDataTarget().setTargetProvisioningObjects(targetDaoRetrieveIncrementalDataResponse.getTargetData());
      }

      long retrieveDataPass2 = System.currentTimeMillis()-start;
      // if full dont log this
      if (retrieveDataPass2 > 1) {
        debugMap.put("retrieveDataPass2_millis", retrieveDataPass2);
      }
    } finally {
      // TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveDataPass2);
    }

    debugMap.put("state", "indexMatchingIdOfTargetObjects");
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdOfTargetObjects();

    try {
      debugMap.put("state", "compareTargetObjects");
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
    }
    
    this.countInsertsUpdatesDeletes();

    try {
      debugMap.put("state", "sendChangesToTarget");
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
      targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
      targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
      targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
    } finally {
      //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.sendChangesToTarget);
    }
  
    {
      // do this in the right spot, after assigning correct sync info about sync
      int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
  
      this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
    }
    
    // TODO flesh this out, resolve subjects, linked cached data, etc, try individually again
//    this.getGrouperProvisioner().retrieveTargetDao().resolveErrors();
//    this.getGrouperProvisioner().retrieveTargetDao().sendErrorFixesToTarget();

//    this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedGroupsToTarget();
//    this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedMembershipsToTarget();

    // make sure the sync objects are correct
//    new ProvisioningSyncIntegration().assignTarget(this.getGrouperProvisioner().getConfigId()).fullSync();

//    // step 1
//    debugMap.put("state", "retrieveData");
//    this.gcTableSyncConfiguration.getGcTableSyncSubtype().retrieveData(debugMap, this);
//    
//    this.gcGrouperSyncLog.setRecordsProcessed(Math.max(this.gcTableSyncOutput.getRowsSelectedFrom(), this.gcTableSyncOutput.getRowsSelectedTo()));
//
//    if (this.gcGrouperSyncHeartbeat.isInterrupted()) {
//      debugMap.put("interrupted", true);
//      debugMap.put("state", "done");
//      gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.INTERRUPTED);
//      return;
//    }

  }

  public void createMissingGroupsFull() {
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsInsert(), false)) {
      return;
    }
      
    //do we have missing groups?
    List<ProvisioningGroup> missingGroups = new ArrayList<ProvisioningGroup>();
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups())) {
      
      // shouldnt be null at this point
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup();

      if (!gcGrouperSyncGroup.isProvisionable()) {
        continue;
      }

      ProvisioningGroup targetGroup = provisioningGroup.getProvisioningGroupWrapper().getTargetProvisioningGroup();
      
      if (targetGroup != null) {
        continue;
      }
      
      missingGroups.add(provisioningGroup);
    }

    if (GrouperUtil.length(missingGroups) == 0) {
      return;
    }

    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingGroupsForCreate", GrouperUtil.length(missingGroups));
    
    this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().setProvisioningGroups(missingGroups);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroupsForCreate);

    List<ProvisioningGroup> grouperTargetGroupsToInsert = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(missingGroups, false, true);

    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroupsToInsert);
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroupsToInsert);

    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroupsToInsert);
    // add object change entries
    this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForGroupsToInsert(grouperTargetGroupsToInsert);
    
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGrouperTargetGroupsForCreate);

    //lets create these
    RuntimeException runtimeException = null;
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(grouperTargetGroupsToInsert));
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertGroups(grouperTargetGroupsToInsert, false);
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }
    }
    
    //retrieve so we have a copy
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToInsert, false));
    
    List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());

    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroups(targetGroups, true, false, false);
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(targetGroups);

    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(targetGroups);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingCreated().setProvisioningGroups(targetGroups);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsCreated);

    Map<Object, ProvisioningGroupWrapper> matchingIdToProvisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper();
    
    // match these up with retrieved groups
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      
      // look up the grouper group that looked this up
      ProvisioningGroupWrapper provisioningGroupWrapper = matchingIdToProvisioningGroupWrapper.get(targetGroup.getMatchingId());
      
      // not sure why it wouldnt match or exist...
      provisioningGroupWrapper.setTargetProvisioningGroup(targetGroup);
      targetGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
    }
    
  }

  public void createMissingEntitiesFull() {
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntitiesInsert(), false)) {
      return;
    }
      
    
    //do we have missing entities?
    List<ProvisioningEntity> missingEntities = new ArrayList<ProvisioningEntity>();
    
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningEntities())) {
      
      // shouldnt be null at this point
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember();
      
      if (!gcGrouperSyncMember.isProvisionable()) {
        continue;
      }
      
      ProvisioningEntity targetEntity = provisioningEntity.getProvisioningEntityWrapper().getTargetProvisioningEntity();
      
      if (targetEntity != null) {
        continue;
      }

      missingEntities.add(provisioningEntity);
    }

    if (GrouperUtil.length(missingEntities) == 0) {
      return;
    }
    
    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingEntitiesForCreate", GrouperUtil.length(missingEntities));
    
    this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().setProvisioningEntities(missingEntities);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingEntitiesForCreate);
    
    // translate
    List<ProvisioningEntity> grouperTargetEntitiesToInsert = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(missingEntities, false, true);
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningEntities(grouperTargetEntitiesToInsert);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGrouperTargetEntitiesForCreate);

    // add object change entries
    this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForEntitiesToInsert(grouperTargetEntitiesToInsert);
    
    //lets create these
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertEntities(new TargetDaoInsertEntitiesRequest(grouperTargetEntitiesToInsert));
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertEntities(grouperTargetEntitiesToInsert, false);
      } catch (Exception e) {
        LOG.error(e);
      }
    }

    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(grouperTargetEntitiesToInsert);

    //retrieve so we have a copy
    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToInsert, false));
    
    List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
    
    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(targetEntities);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingCreated().setProvisioningEntities(targetEntities);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetEntitiesCreated);

    // match these up with retrieved entities
    Map<Object, ProvisioningEntityWrapper> matchingIdToProvisioningEntityWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper();
    
    // match these up with retrieved groups
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(targetEntities)) {
      
      // look up the grouper group that looked this up
      ProvisioningEntityWrapper provisioningEntityWrapper = matchingIdToProvisioningEntityWrapper.get(targetEntity.getMatchingId());
      
      // not sure why it wouldnt match or exist...
      provisioningEntityWrapper.setTargetProvisioningEntity(targetEntity);
      targetEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);

    }

  }

  public void setupIncrementalGrouperTargetObjectsToRetrieveFromTarget() {

    TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest();

    GrouperIncrementalDataToProcess grouperIncrementalUuidsToRetrieveFromGrouper =
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();

    {
      //the groups only should be the full list of grouper target groups
      List<ProvisioningGroup> grouperTargetGroupsForGroupOnly = new ArrayList<ProvisioningGroup>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsIncludeDeletes().getProvisioningGroups()));
      targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(grouperTargetGroupsForGroupOnly);
    }

    {
      // generally these arent needed, but if there are no target groups (e.g. only entities or memberships), maybe needed
      List<ProvisioningGroupWrapper> groupWrappersForGroupOnly = new ArrayList<ProvisioningGroupWrapper>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper()).values());
      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupWrappersForGroupOnly(groupWrappersForGroupOnly);
    }
    {
      //the entities only should be the full list of grouper target entities
      List<ProvisioningEntity> grouperTargetEntitiesForEntityOnly = new ArrayList<ProvisioningEntity>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsIncludeDeletes().getProvisioningEntities()));
      targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(grouperTargetEntitiesForEntityOnly);
    }
    {
      // generally these arent needed, but if there are no target entities (e.g. only groups or memberships), maybe needed
      List<ProvisioningEntityWrapper> entityWrappersForEntityOnly = new ArrayList<ProvisioningEntityWrapper>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper()).values());
      targetDaoRetrieveIncrementalDataRequest.setProvisioningEntityWrappersForEntityOnly(entityWrappersForEntityOnly);
    }
    {
      int missingGrouperTargetGroupsForGroupSync = 0;
      //the groups for group memberships should be looked up from the wrapper objects
      List<ProvisioningGroup> grouperTargetGroupForGroupSync = new ArrayList<ProvisioningGroup>();
      List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupSync = new ArrayList<ProvisioningGroupWrapper>();
      targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupMembershipSync(grouperTargetGroupForGroupSync);
      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupWrappersForGroupMembershipSync(provisioningGroupWrappersForGroupSync);
      for (String groupUuid : GrouperUtil.nonNull(grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupMembershipSync())) {
        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupUuid);
        if (provisioningGroupWrapper == null) {
          missingGrouperTargetGroupsForGroupSync++;
          continue;
        }
        provisioningGroupWrappersForGroupSync.add(provisioningGroupWrapper);
        ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
        if (grouperTargetGroup == null || !provisioningGroupWrapper.isDelete()) {
          // this might be expected
          continue;
        }
        grouperTargetGroupForGroupSync.add(grouperTargetGroup);
      }
      if (missingGrouperTargetGroupsForGroupSync > 0) {
        this.grouperProvisioner.getDebugMap().put("missingGrouperTargetGroupsForGroupSync", missingGrouperTargetGroupsForGroupSync);
      }
    }    
    {
      int missingGrouperTargetEntitiesForEntitySync = 0;
      //the groups for group memberships should be looked up from the wrapper objects
      List<ProvisioningEntity> grouperTargetEntityForEntitySync = new ArrayList<ProvisioningEntity>();
      List<ProvisioningEntityWrapper> provisioningEntityWrappersForEntitySync = new ArrayList<ProvisioningEntityWrapper>();
      
      targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityMembershipSync(grouperTargetEntityForEntitySync);
      targetDaoRetrieveIncrementalDataRequest.setProvisioningEntityWrappersforEntityMembershipSync(provisioningEntityWrappersForEntitySync);

      for (String memberUuid : GrouperUtil.nonNull(grouperIncrementalUuidsToRetrieveFromGrouper.getMemberUuidsForEntityMembershipSync())) {
        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberUuid);
        if (provisioningEntityWrapper == null) {
          missingGrouperTargetEntitiesForEntitySync++;
          continue;
        }
        provisioningEntityWrappersForEntitySync.add(provisioningEntityWrapper);
        
        ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
        if (grouperTargetEntity == null || !provisioningEntityWrapper.isDelete()) {
          continue;
        }
        grouperTargetEntityForEntitySync.add(grouperTargetEntity);
      }
      if (missingGrouperTargetEntitiesForEntitySync > 0) {
        this.grouperProvisioner.getDebugMap().put("missingGrouperTargetEntitiesForEntitySync", missingGrouperTargetEntitiesForEntitySync);
      }
    }    
    //lets get the memberships
    {
      int missingGrouperTargetMembershipsForMembershipSync = 0;
      //the groups only should be the full list of grouper target groups
      List<MultiKey> groupEntityMembershipWrappers = new ArrayList<MultiKey>();
      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupMemberMembershipWrappersForMembershipSync(groupEntityMembershipWrappers);

      List<MultiKey> grouperTargetGroupsGrouperTargetEntitiesGrouperTargetMemberships = new ArrayList<MultiKey>();
      targetDaoRetrieveIncrementalDataRequest
        .setTargetGroupsEntitiesMembershipsForMembershipSync(grouperTargetGroupsGrouperTargetEntitiesGrouperTargetMemberships);
      
      //get the ones that were looked up in grouper
      for (MultiKey groupUuidMemberUuidFieldId : GrouperUtil.nonNull(grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsMemberUuidsFieldIdsForMembershipSync())) {
        
        String groupUuid = (String)groupUuidMemberUuidFieldId.getKey(0);
        String memberUuid = (String)groupUuidMemberUuidFieldId.getKey(1);
        
        MultiKey groupUuidMemberUuid = new MultiKey(groupUuid, memberUuid);
        
        // dont need field id
        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberUuid);
        ProvisioningEntity grouperTargetEntity = (provisioningEntityWrapper == null || !provisioningEntityWrapper.isDelete()) ? null : provisioningEntityWrapper.getGrouperTargetEntity();

        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupUuid);
        ProvisioningGroup grouperTargetGroup = (provisioningGroupWrapper == null || !provisioningGroupWrapper.isDelete()) ? null : provisioningGroupWrapper.getGrouperTargetGroup();

        ProvisioningMembershipWrapper provisioningMembershipWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(groupUuidMemberUuid);
        ProvisioningMembership grouperTargetMembership = (provisioningMembershipWrapper == null || !provisioningMembershipWrapper.isDelete()) ? null : provisioningMembershipWrapper.getGrouperTargetMembership();

        if (grouperTargetMembership != null || (grouperTargetGroup != null && grouperTargetEntity != null)) {
          grouperTargetGroupsGrouperTargetEntitiesGrouperTargetMemberships.add(new MultiKey(grouperTargetGroup, grouperTargetEntity, grouperTargetMembership));
        }
        if (provisioningMembershipWrapper != null || (provisioningGroupWrapper != null && provisioningEntityWrapper != null)) {
          groupEntityMembershipWrappers.add(new MultiKey(provisioningGroupWrapper, provisioningEntityWrapper, provisioningMembershipWrapper));
          
        } else {
          missingGrouperTargetMembershipsForMembershipSync++;
        }
        
      }
      if (missingGrouperTargetMembershipsForMembershipSync > 0) {
        this.grouperProvisioner.getDebugMap().put("missingGrouperTargetMembershipsForMembershipSync", missingGrouperTargetMembershipsForMembershipSync);
      }
      
    }

  }

  /**
   * retrieve all data from both sides, grouper and target, do this in a thread
   */
  public void retrieveAllData() {
    
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    
    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          TargetDaoRetrieveAllDataResponse targetDaoRetrieveAllDataResponse
            = GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter()
              .retrieveAllData(new TargetDaoRetrieveAllDataRequest());
          // retrieve all the target data and put in GrouperProvisioningDataTarget
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningDataTarget()
            .setTargetProvisioningObjects(targetDaoRetrieveAllDataResponse.getTargetData());
        } catch (RuntimeException re) {
          LOG.error("error querying target: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();

    retrieveGrouperDataFull();
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }
  }


  public void retrieveGrouperDataFull() {
    final RuntimeException[] RUNTIME_EXCEPTION2 = new RuntimeException[1];
    
    Thread grouperSyncQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          // retrieve all grouper sync data and put in GrouperProvisioningDataSync
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperSyncDao().retrieveSyncDataFull();
        } catch (RuntimeException re) {
          LOG.error("error querying sync objects: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION2[0] = re;
        }
        
      }
    });

    grouperSyncQueryThread.start();
    
    // get all grouper data for the provisioner
    // and put in GrouperProvisioningDataSyncGrouper
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataFull();
    
    // put wrappers on the grouper objects and put in the grouper uuid maps in data object
    // put these wrapper in the GrouperProvisioningData and GrouperProvisioningDataIndex
    this.grouperProvisioner.retrieveGrouperDao().processWrappers();
    
    // point the membership pointers to groups and entities to what they should point to
    // and fix data problems (for instance race conditions as data was retrieved)
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();
    
    GrouperClientUtils.join(grouperSyncQueryThread);
    if (RUNTIME_EXCEPTION2[0] != null) {
      throw RUNTIME_EXCEPTION2[0];
    }

    // add / update / delete sync objects based on grouper data
    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();

    // put the sync objects in their respective wrapper objects
    assignSyncObjectsToWrappers();

    GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
    gcGrouperSync.setGroupCount(GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups()));
    gcGrouperSync.setUserCount(GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningEntities()));
    gcGrouperSync.setRecordsCount(
        GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningEntities())
        + GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups())
        + GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningMemberships())
        );
    
  }


  public void assignSyncObjectsToWrappers() {
    
    Map<String, GcGrouperSyncGroup> syncIdToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
    Map<String, GcGrouperSyncMember> syncIdToSyncMember = new HashMap<String, GcGrouperSyncMember>();
    
    {
      Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();

      Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper();

      // loop through sync groups
      for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncGroups())) {
    
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(gcGrouperSyncGroup.getGroupId());
        
        if (provisioningGroupWrapper == null) {
          provisioningGroupWrapper = new ProvisioningGroupWrapper();
          groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
        }
        provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
        
        syncIdToSyncGroup.put(gcGrouperSyncGroup.getId(), gcGrouperSyncGroup);
        grouperSyncGroupIdToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getId(), provisioningGroupWrapper);
      }
    }

    {
      Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();

      Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper();

      // loop through sync groups
      for (GcGrouperSyncMember gcGrouperSyncMember : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncMembers())) {
    
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(gcGrouperSyncMember.getMemberId());
        
        if (provisioningEntityWrapper == null) {
          provisioningEntityWrapper = new ProvisioningEntityWrapper();
          memberUuidToProvisioningEntityWrapper.put(gcGrouperSyncMember.getMemberId(), provisioningEntityWrapper);
        }
        provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);

        syncIdToSyncMember.put(gcGrouperSyncMember.getId(), gcGrouperSyncMember);
        grouperSyncMemberIdToProvisioningEntityWrapper.put(gcGrouperSyncMember.getId(), provisioningEntityWrapper);
      }
    }

    {
      Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

      List<GcGrouperSyncMembership> gcGrouperSyncMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncMemberships();

      Map<MultiKey, ProvisioningMembershipWrapper> grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper();

      if (GrouperUtil.length(gcGrouperSyncMemberships) > 0) {
        
        int syncMembershipReferenceMissing = 0;
        
        for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMemberships) {
          
          // data is not consistent just ignore for now
          GcGrouperSyncGroup gcGrouperSyncGroup = syncIdToSyncGroup.get(gcGrouperSyncMembership.getGrouperSyncGroupId());
          if (gcGrouperSyncGroup == null) {
            syncMembershipReferenceMissing++;
            continue;
          }
          
          GcGrouperSyncMember gcGrouperSyncMember = syncIdToSyncMember.get(gcGrouperSyncMembership.getGrouperSyncMemberId());
          
          if (gcGrouperSyncMember == null) {
            syncMembershipReferenceMissing++;
            continue;
          }
          MultiKey groupIdMemberId = new MultiKey(gcGrouperSyncGroup.getGroupId(),
              gcGrouperSyncMember.getMemberId());
          
          ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
          
          if (provisioningMembershipWrapper == null) {
            provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
            groupUuidMemberUuidToProvisioningMembershipWrapper.put(groupIdMemberId, provisioningMembershipWrapper);
          }
          
          provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
        }
        if (syncMembershipReferenceMissing > 0) {
          this.getGrouperProvisioner().getDebugMap().put("syncMembershipReferenceMissing", syncMembershipReferenceMissing);
        }
      }
      
    }
  }
  
  public void retrieveGrouperDataIncremental() {
    final RuntimeException[] RUNTIME_EXCEPTION2 = new RuntimeException[1];
    
    Thread grouperSyncQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperSyncDao().retrieveSyncDataIncremental();
        } catch (RuntimeException re) {
          LOG.error("error querying sync objects: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION2[0] = re;
        }
        
      }
    });

    grouperSyncQueryThread.start();
    
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataIncremental();
    this.grouperProvisioner.retrieveGrouperDao().processWrappers();
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();
    
    // incrementals need to clone and setup sync objects as deletes
    setupIncrementalClonesOfGroupProvisioningObjects();

    GrouperClientUtils.join(grouperSyncQueryThread);
    if (RUNTIME_EXCEPTION2[0] != null) {
      throw RUNTIME_EXCEPTION2[0];
    }

    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();

    // incrementals need to consult sync objects to know what to delete
    calculateProvisioningDataToDelete(); 
    
    assignSyncObjectsToWrappers();

  }
  
  protected void countInsertsUpdatesDeletes() {
    
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships());
    
  }
  
  protected void countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction provisioningObjectChangeAction, List<? extends ProvisioningUpdatable> provisioningUpdatables) {
    // maybe not count fields?
    if (provisioningUpdatables == null) {
      return;
    }
    switch(provisioningObjectChangeAction) {
      case insert:
        this.grouperProvisioner.getGrouperProvisioningOutput().addInsert(GrouperUtil.length(provisioningUpdatables));  
        break;
      case update:
        this.grouperProvisioner.getGrouperProvisioningOutput().addUpdate(GrouperUtil.length(provisioningUpdatables));  
        break;
      case delete:
        this.grouperProvisioner.getGrouperProvisioningOutput().addDelete(GrouperUtil.length(provisioningUpdatables));  
        break;
    }
    for (ProvisioningUpdatable provisioningUpdatable : provisioningUpdatables) {
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(provisioningUpdatable.getInternal_objectChanges())) {
        switch (provisioningObjectChange.getProvisioningObjectChangeAction()) {
          case insert:
            this.grouperProvisioner.getGrouperProvisioningOutput().addInsert(1);  
            break;
          case update:
            this.grouperProvisioner.getGrouperProvisioningOutput().addUpdate(1);  
            break;
          case delete:
            this.grouperProvisioner.getGrouperProvisioningOutput().addDelete(1);  
            break;
          
        }
      }
    }
  }
  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningLogic.class);

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
   * make a deep copy of grouper provisioning data into the grouper provisioning objects to delete
   */
  public void setupIncrementalClonesOfGroupProvisioningObjects() {
    GrouperProvisioningLists grouperProvisioningObjects = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects();
    GrouperProvisioningLists grouperProvisioningObjectsIncludeDeletes = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsIncludeDeletes();
  
    List<ProvisioningGroup> grouperProvisioningGroupsIncludeDeletes = (List<ProvisioningGroup>)(Object)
        ProvisioningUpdatable.clone((List<ProvisioningUpdatable>)(Object)grouperProvisioningObjects.getProvisioningGroups());
    grouperProvisioningObjectsIncludeDeletes.setProvisioningGroups(grouperProvisioningGroupsIncludeDeletes);

    List<ProvisioningEntity> grouperProvisioningEntitysIncludeDeletes = (List<ProvisioningEntity>)(Object)
        ProvisioningUpdatable.clone((List<ProvisioningUpdatable>)(Object)grouperProvisioningObjects.getProvisioningEntities());
    grouperProvisioningObjectsIncludeDeletes.setProvisioningEntities(grouperProvisioningEntitysIncludeDeletes);

    List<ProvisioningMembership> grouperProvisioningMembershipsIncludeDeletes = (List<ProvisioningMembership>)(Object)
        ProvisioningUpdatable.clone((List<ProvisioningUpdatable>)(Object)grouperProvisioningObjects.getProvisioningMemberships());
    grouperProvisioningObjectsIncludeDeletes.setProvisioningMemberships(grouperProvisioningMembershipsIncludeDeletes);

    Map<String, ProvisioningGroup> groupUuidToProvisioningGroupIncludeDelete = new HashMap<String, ProvisioningGroup>();
    for (ProvisioningGroup provisioningGroupIncludeDelete : GrouperUtil.nonNull(grouperProvisioningGroupsIncludeDeletes)) {
      groupUuidToProvisioningGroupIncludeDelete.put(provisioningGroupIncludeDelete.getId(), provisioningGroupIncludeDelete);
      ProvisioningGroupWrapper provisioningGroupWrapper = provisioningGroupIncludeDelete.getProvisioningGroupWrapper();
      provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroupIncludeDelete);
      provisioningGroupWrapper.setDelete(true);
    }

    Map<String, ProvisioningEntity> memberUuidToProvisioningEntityIncludeDelete = new HashMap<String, ProvisioningEntity>();
    for (ProvisioningEntity provisioningEntityIncludeDelete : GrouperUtil.nonNull(grouperProvisioningEntitysIncludeDeletes)) {
      memberUuidToProvisioningEntityIncludeDelete.put(provisioningEntityIncludeDelete.getId(), provisioningEntityIncludeDelete);
      ProvisioningEntityWrapper provisioningEntityWrapper = provisioningEntityIncludeDelete.getProvisioningEntityWrapper();
      provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningEntityIncludeDelete);
      provisioningEntityWrapper.setDelete(true);
    }

    int membershipReferenceNotMatchIncludeDeletes = 0;
    for (ProvisioningMembership provisioningMembershipIncludeDelete : GrouperUtil.nonNull(grouperProvisioningMembershipsIncludeDeletes)) {
      
      {
        ProvisioningGroup grouperProvisioningGroupIncludeDelete = groupUuidToProvisioningGroupIncludeDelete.get(provisioningMembershipIncludeDelete.getProvisioningGroupId());
        if (grouperProvisioningGroupIncludeDelete == null) {
          membershipReferenceNotMatchIncludeDeletes++;
        }
        provisioningMembershipIncludeDelete.setProvisioningGroup(grouperProvisioningGroupIncludeDelete);
      }
      {
        ProvisioningEntity grouperProvisioningEntityIncludeDelete = memberUuidToProvisioningEntityIncludeDelete.get(provisioningMembershipIncludeDelete.getProvisioningEntityId());
        if (grouperProvisioningEntityIncludeDelete == null) {
          membershipReferenceNotMatchIncludeDeletes++;
        }
        provisioningMembershipIncludeDelete.setProvisioningEntity(grouperProvisioningEntityIncludeDelete);
      }
      ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningMembershipIncludeDelete.getProvisioningMembershipWrapper();
      provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembershipIncludeDelete);
      provisioningMembershipWrapper.setDelete(true);

    }
    if (membershipReferenceNotMatchIncludeDeletes > 0) {
      this.grouperProvisioner.getDebugMap().put("membershipReferenceNotMatchIncludeDeletes", membershipReferenceNotMatchIncludeDeletes);
    }
  }


  public void calculateProvisioningDataToDelete() {
    this.calculateProvisioningGroupsToDelete();
    this.calculateProvisioningEntitiesToDelete();
    this.calculateProvisioningMembershipsToDelete();
    
  }


  /**
   * take the sync members and see which ones do not correspond to a grouper member
   */
  public void calculateProvisioningEntitiesToDelete() {
  
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningMemberWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
  
    int provisioningEntitiesToDelete = 0;
  
    // loop through sync groups
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
  
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      ProvisioningEntity provisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      
      // if a entity has been deleted in grouper_members table but copy still exists in grouper_sync_member
      // we are sending the copy over to the target so that target can also delete
      if (provisioningEntity == null) {
        
        provisioningEntitiesToDelete++;
        
        provisioningEntity = new ProvisioningEntity();
        provisioningEntity.setId(gcGrouperSyncMember.getMemberId());
  
        provisioningEntity.assignAttributeValue("subjectId", gcGrouperSyncMember.getSubjectId());
        provisioningEntity.assignAttributeValue("subjectIdentifier0", gcGrouperSyncMember.getSubjectIdentifier());
  
        provisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
        
        memberUuidToProvisioningMemberWrapper.put(provisioningEntity.getId(), provisioningEntityWrapper);
      }
        
      provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
      
    }
    
    if (provisioningEntitiesToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningEntitiesToDelete", provisioningEntitiesToDelete);
    }
  
  }


  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningGroupsToDelete() {
  
    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().values()) {
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      if (gcGrouperSyncGroup != null) {
        groupUuidToSyncGroup.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
      }
    }
  
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
  
    int provisioningGroupsToDeleteCount = 0;
  
    List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups();
  
    // loop through sync groups
    for (GcGrouperSyncGroup gcGrouperSyncGroup : groupUuidToSyncGroup.values()) {
  
      ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(gcGrouperSyncGroup.getGroupId());
      
      // if a group has been deleted in grouper_groups table but copy still exists in grouper_sync_group
      // we are sending the copy over to the target so that target can also delete
      if (provisioningGroupWrapper == null) {
        
        provisioningGroupsToDeleteCount++;
        
        // create a provisioning group to delete
        ProvisioningGroup provisioningGroup = new ProvisioningGroup();
        provisioningGroup.setId(gcGrouperSyncGroup.getGroupId());
        provisioningGroup.setName(gcGrouperSyncGroup.getGroupName());
        provisioningGroup.setIdIndex(gcGrouperSyncGroup.getGroupIdIndex());
        if (grouperProvisioningGroups == null) {
          grouperProvisioningGroups = new ArrayList<ProvisioningGroup>();
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().setProvisioningGroups(grouperProvisioningGroups);
        }
        grouperProvisioningGroups.add(provisioningGroup);
        
        provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
  
        provisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
        provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroup);
        provisioningGroupWrapper.setDelete(true);
        
        groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
        
      }
      provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
      
    }
    if (provisioningGroupsToDeleteCount > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupsToDeleteCount", provisioningGroupsToDeleteCount);
    }
  
  }


  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningMembershipsToDelete() {
  
    Map<String, GcGrouperSyncMember> memberUuidToSyncMember = new HashMap<String, GcGrouperSyncMember>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().values()) {
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      if (gcGrouperSyncMember != null) {
        memberUuidToSyncMember.put(gcGrouperSyncMember.getMemberId(), gcGrouperSyncMember);
      }
    }

    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().values()) {
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      if (gcGrouperSyncGroup != null) {
        groupUuidToSyncGroup.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
      }
    }
    
    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncMemberships();
  
    List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper()
        .getGrouperProvisioningObjects().getProvisioningMemberships();
    
    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner()
        .retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
  
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
  
    int provisioningMshipsToDelete = 0;
    
    // loop through sync groups
    for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperUtil.nonNull(gcGrouperSyncMemberships)) {

      GcGrouperSyncGroup gcGrouperSyncGroup = groupUuidToSyncGroup.get(gcGrouperSyncMembership.getGrouperSyncGroupId());
      GcGrouperSyncMember gcGrouperSyncMember = memberUuidToSyncMember.get(gcGrouperSyncMembership.getGrouperSyncMemberId());
      
      String groupId = gcGrouperSyncGroup.getGroupId();
      String memberId = gcGrouperSyncMember.getMemberId();
  
      MultiKey groupUuidMemberUuid = new MultiKey(groupId, memberId);
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupUuidMemberUuid);
      
      // if a group has been deleted in grouper_groups table but copy still exists in grouper_sync_group
      // we are sending the copy over to the target so that target can also delete
      if (provisioningMembershipWrapper == null) {
        
        provisioningMshipsToDelete++;
        
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(groupId);
        if (provisioningGroupWrapper == null) {
          throw new RuntimeException("Cant find groupId: '" + groupId + "'");
        }
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(memberId);
        if (provisioningEntityWrapper == null) {
          throw new RuntimeException("Cant find entityId: '" + memberId + "'");
        }
        
        // create a provisioning group to delete
        ProvisioningMembership provisioningMembership = new ProvisioningMembership();
        provisioningMembership.setProvisioningGroupId(groupId);
        provisioningMembership.setProvisioningEntityId(memberId);
        
        // the group is either the provisioning group or provisioning group to delete
        if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
          provisioningMembership.setProvisioningGroup(provisioningGroupWrapper.getGrouperProvisioningGroup());
        } else {
          throw new RuntimeException("Cant find provisioning group: '" + groupId + "'");
        }
  
        // the group is either the provisioning group or provisioning group to delete
        if (provisioningEntityWrapper.getGrouperProvisioningEntity() != null) {
          provisioningMembership.setProvisioningEntity(provisioningEntityWrapper.getGrouperProvisioningEntity());
        } else {
          throw new RuntimeException("Cant find provisioning entity: '" + memberId + "'");
        }
  
        if (grouperProvisioningMemberships == null) {
          grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>();
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().setProvisioningMemberships(grouperProvisioningMemberships);
        }
        grouperProvisioningMemberships.add(provisioningMembership);
        
        provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembership);
        provisioningMembershipWrapper.setDelete(true);
        
        groupUuidMemberUuidToProvisioningMembershipWrapper.put(groupUuidMemberUuid, provisioningMembershipWrapper);
        
      }
      provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
    }      
    if (provisioningMshipsToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningMshipsToDelete", provisioningMshipsToDelete);
    }
  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingObjectsIncremental() {
    retrieveMissingGroupsIncremental();
    retrieveMissingEntitiesIncremental();
  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingGroupsIncremental() {
    
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsRetrieveMissingIncremental(), false)) {
      return;
    }
      
    //do we have missing groups?
    List<ProvisioningGroup> missingGroups = new ArrayList<ProvisioningGroup>();
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups())) {
      
      // shouldnt be null at this point
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup();
      
      if (!gcGrouperSyncGroup.isProvisionable()) {
        continue;
      }
      if (this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().groupLinkMissing(gcGrouperSyncGroup)) {
        missingGroups.add(provisioningGroup);
      }
    }

    if (GrouperUtil.length(missingGroups) == 0) {
      return;
    }
    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingIncrementalGroupsForRetrieve", missingGroups);
    
    this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().setProvisioningGroups(missingGroups);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroups);

    // translate
    List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(missingGroups, false, false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroups);

    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroups);
    
    // log this
    //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroups);
    
    //lets retrieve these
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroups, false));
    
    List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
    
    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(targetGroups);

    this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingRetrieved().setProvisioningGroups(targetGroups);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsRetrieved);
    
    Map<Object, ProvisioningGroup> matchingIdToGrouperTargetGroup = new HashMap<Object, ProvisioningGroup>();
    
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroups)) {
      matchingIdToGrouperTargetGroup.put(grouperTargetGroup.getMatchingId(), grouperTargetGroup);
    }
    
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      
      // look up the grouper group that looked this up
      ProvisioningGroup grouperTargetGroup = matchingIdToGrouperTargetGroup.get(targetGroup.getMatchingId());
      
      // not sure why it wouldnt match or exist...
      grouperTargetGroup.getProvisioningGroupWrapper().setTargetProvisioningGroup(targetGroup);
    }

  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingEntitiesIncremental() {
    
  }

}
