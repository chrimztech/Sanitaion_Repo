package com.unza.ubirr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ldap")
public class LdapProperties {

  private boolean enabled = false;
  private String url = "ldap://localhost:389";
  private String baseDn = "";
  private String userDnPattern = "uid={0},ou=People";
  private String userSearchBase = "ou=People";
  private String userSearchFilter = "(uid={0})";
  private String emailAttribute = "mail";
  private String nameAttribute = "cn";
  private String managerDn = "";
  private String managerPassword = "";

  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public String getBaseDn() { return baseDn; }
  public void setBaseDn(String baseDn) { this.baseDn = baseDn; }

  public String getUserDnPattern() { return userDnPattern; }
  public void setUserDnPattern(String userDnPattern) { this.userDnPattern = userDnPattern; }

  public String getUserSearchBase() { return userSearchBase; }
  public void setUserSearchBase(String userSearchBase) { this.userSearchBase = userSearchBase; }

  public String getUserSearchFilter() { return userSearchFilter; }
  public void setUserSearchFilter(String userSearchFilter) { this.userSearchFilter = userSearchFilter; }

  public String getEmailAttribute() { return emailAttribute; }
  public void setEmailAttribute(String emailAttribute) { this.emailAttribute = emailAttribute; }

  public String getNameAttribute() { return nameAttribute; }
  public void setNameAttribute(String nameAttribute) { this.nameAttribute = nameAttribute; }

  public String getManagerDn() { return managerDn; }
  public void setManagerDn(String managerDn) { this.managerDn = managerDn; }

  public String getManagerPassword() { return managerPassword; }
  public void setManagerPassword(String managerPassword) { this.managerPassword = managerPassword; }
}
