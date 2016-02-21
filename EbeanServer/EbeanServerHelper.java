package com.quikr.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Campaign;
import models.LeadReferralDetails;
import models.Micromarket;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class EbeanServerHelper {

  private static final Config ebeanConfig = ConfigFactory.load("ebean.conf");

  private static List<String> EBEAN_SERVER = null;// ,Arrays.asList(new
  static {
    try {
      EBEAN_SERVER = ebeanConfig.getStringList("EbeanServers");
    } catch (Exception e) {
    }
  }

  public enum EbeanServerName {
   
    QUIKRLOG_READ("ebean.quikrlog_read"),
    QUIKRLOG_WRITE("ebean.quikrlog_write");

    private String serverName;

    public String getServerName() {
      return serverName;
    }

    public String getDbUrl() {
      return ebeanConfig.getString(serverName + ".url");
    }

    public String getUsername() {
      return ebeanConfig.getString(serverName + ".user");
    }

    public String getPassword() {
      return ebeanConfig.getString(serverName + ".password");
    }

    public int getMinConnections() {
      return ebeanConfig.getInt(serverName + ".minConnectionsPerPartition");
    }

    public int getMaxConnections() {
      return ebeanConfig.getInt(serverName + ".maxConnectionsPerPartition");
    }

    EbeanServerName(String serverName) {
      this.serverName = serverName;
    }

  }

  private static Map<EbeanServerName, EbeanServer> ebeanServers = createEbeanServers();

  // private static final List<class>
  private static Map<EbeanServerName, EbeanServer> createEbeanServers() {
    Map<EbeanServerName, EbeanServer> ebeanServers = new HashMap<EbeanServerName, EbeanServer>();

    createQuikrLogReadEbeanServer(ebeanServers);
    createQuikrLogWriteEbeanServer(ebeanServers);

    return ebeanServers;
  }

  /**
   * Ad Classes for all the new data types which are to be added in KIJIJI server
   */



  private static void createQuikrLogReadEbeanServer(Map<EbeanServerName, EbeanServer> ebeanServers) {
    if (EBEAN_SERVER == null
            || EBEAN_SERVER.contains(EbeanServerName.QUIKRLOG_READ.getServerName())) {

      ServerConfig config = createConfig(EbeanServerName.QUIKRLOG_READ);
      addQuikrLogEbeanClasses(config);
      EbeanServer server = EbeanServerFactory.create(config);
      ebeanServers.put(EbeanServerName.QUIKRLOG_READ, server);
    }

  }

  private static void
          createQuikrLogWriteEbeanServer(Map<EbeanServerName, EbeanServer> ebeanServers) {
    if (EBEAN_SERVER == null
            || EBEAN_SERVER.contains(EbeanServerName.QUIKRLOG_WRITE.getServerName())) {

      ServerConfig config = createConfig(EbeanServerName.QUIKRLOG_WRITE);
      addQuikrLogEbeanClasses(config);
      EbeanServer server = EbeanServerFactory.create(config);
      ebeanServers.put(EbeanServerName.QUIKRLOG_WRITE, server);
    }
  }

  private static void addQuikrLogEbeanClasses(ServerConfig config) {
   
    config.addClass(Campaign.class);
    config.addClass(LeadReferralDetails.class);
    config.addClass(Micromarket.class);
  }




  private static ServerConfig createConfig(EbeanServerName ebeanServerName) {
    ServerConfig config = new ServerConfig();
    config.setName(ebeanServerName.getServerName());
    DataSourceConfig mySQLDataSource = createDataSourceConfig(ebeanServerName);
    config.setDataSourceConfig(mySQLDataSource);
    config.setDefaultServer(false);
    config.setRegister(false);
    return config;
  }

  private static DataSourceConfig createDataSourceConfig(EbeanServerName ebeanServerName) {
    DataSourceConfig mySQLDataSource = new DataSourceConfig();
    mySQLDataSource.setDriver("com.mysql.jdbc.Driver");
    mySQLDataSource.setUrl(ebeanServerName.getDbUrl());
    mySQLDataSource.setUsername(ebeanServerName.getUsername());
    mySQLDataSource.setPassword(ebeanServerName.getPassword());
    mySQLDataSource.setMinConnections(ebeanServerName.getMinConnections());
    mySQLDataSource.setMaxConnections(ebeanServerName.getMaxConnections());
    mySQLDataSource.setIsolationLevel(4);
    mySQLDataSource.setMaxInactiveTimeSecs(1800);
    mySQLDataSource.setHeartbeatFreqSecs(10);
    return mySQLDataSource;
  }

  public static EbeanServer getEbeanServer(EbeanServerName ebeanServerName) {
    return ebeanServers.get(ebeanServerName);
  }

}
