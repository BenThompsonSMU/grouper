/**
 * @author Kate
 * $Id: BundleBean.java,v 1.1 2009-09-09 15:10:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.util;

import javax.servlet.jsp.jstl.fmt.LocalizationContext;


/**
 * bean to hold the resource bundles and maps of bundles
 */
public class BundleBean {

  /** nav.properties resource bundle */
  private LocalizationContext nav;
  
  /** media.properties resource bundle */
  private LocalizationContext media;
  
  /** nav.properties resource bundle as map */
  private MapBundleWrapper navMap;
  
  /** nav.properties resource bundle as map, with null if not there instead of ??? */
  private MapBundleWrapper navMapNull;

  /** media.properties resource bundle as map */
  private MapBundleWrapper mediaMap;

  /** media.properties resource bundle as map with null if not there instead of ??? */
  private MapBundleWrapper mediaMapNull;

  
  /**
   * nav.properties resource bundle
   * @return the nav
   */
  public LocalizationContext getNav() {
    return this.nav;
  }

  
  /**
   * nav.properties resource bundle
   * @param nav1 the nav to set
   */
  public void setNav(LocalizationContext nav1) {
    this.nav = nav1;
  }

  
  /**
   * media.properties resource bundle
   * @return the media
   */
  public LocalizationContext getMedia() {
    return this.media;
  }

  
  /**
   * media.properties resource bundle
   * @param media1 the media to set
   */
  public void setMedia(LocalizationContext media1) {
    this.media = media1;
  }

  
  /**
   * nav.properties resource bundle as map
   * @return the navMap
   */
  public MapBundleWrapper getNavMap() {
    return this.navMap;
  }

  
  /**
   * nav.properties resource bundle as map
   * @param navMap1 the navMap to set
   */
  public void setNavMap(MapBundleWrapper navMap1) {
    this.navMap = navMap1;
  }

  
  /**
   * nav.properties resource bundle as map, with null if not there instead of ???
   * @return the navMapNull
   */
  public MapBundleWrapper getNavMapNull() {
    return this.navMapNull;
  }

  
  /**
   * nav.properties resource bundle as map, with null if not there instead of ???
   * @param navMapNull1 the navMapNull to set
   */
  public void setNavMapNull(MapBundleWrapper navMapNull1) {
    this.navMapNull = navMapNull1;
  }

  
  /**
   * media.properties resource bundle as map
   * @return the mediaMap
   */
  public MapBundleWrapper getMediaMap() {
    return this.mediaMap;
  }

  
  /**
   * media.properties resource bundle as map
   * @param mediaMap1 the mediaMap to set
   */
  public void setMediaMap(MapBundleWrapper mediaMap1) {
    this.mediaMap = mediaMap1;
  }

  
  /**
   * media.properties resource bundle as map with null if not there instead of ???
   * @return the mediaMapNull
   */
  public MapBundleWrapper getMediaMapNull() {
    return this.mediaMapNull;
  }

  
  /**
   * media.properties resource bundle as map with null if not there instead of ???
   * @param mediaMapNull1 the mediaMapNull to set
   */
  public void setMediaMapNull(MapBundleWrapper mediaMapNull1) {
    this.mediaMapNull = mediaMapNull1;
  }
  
}
