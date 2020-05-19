package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonChangeLogTempToChangeLogConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.changeLogTempToChangeLog)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "changeLog.changeLogTempToChangeLog.";
  }
    
  @Override
  public boolean isMultiple() {
    return false;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "CHANGE_LOG_changeLogTempToChangeLog".equals(jobName);
  }
}
