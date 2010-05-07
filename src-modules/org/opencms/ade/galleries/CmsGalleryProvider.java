/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsGalleryProvider.java,v $
 * Date   : $Date: 2010/05/07 13:59:19 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.galleries;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.I_CmsCoreProvider;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Convenience class to provide gallery server-side data to the client.<p> 
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.galleries.client.CmsGalleryProvider
 */
public final class CmsGalleryProvider implements I_CmsGalleryProviderConstants, I_CmsCoreProvider {

    /** Configuration values. */
    public enum GalleryConfiguration {

        /** Tabs configuration as default. */
        TABS_DEFAULT(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name()),

        /** Tabs configuration for the vfs dialogmode. */
        TABS_VIEW(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name()),

        /** Tabs configuration for the ade dialogmode. */
        TABS_ADE(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name()),

        /** Tabs configuration for the sitemap dialogmode. */
        TABS_SITEMAP(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name()
            + ","
            + I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_sitemap.name());

        /** The configuration. */
        private String m_config;

        /** Constructor.<p>
         *
         * @param config the configuration
         */
        private GalleryConfiguration(String config) {

            m_config = config;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getConfig() {

            return m_config;
        }
    }

    /** Internal instance. */
    private static CmsGalleryProvider INSTANCE;

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsGalleryProvider.class);

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private CmsGalleryProvider() {

        // hide the constructor
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsGalleryProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsGalleryProvider();
        }
        return INSTANCE;
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#export(javax.servlet.http.HttpServletRequest)
     */
    public String export(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();
        sb.append(ClientMessages.get().export(request));
        sb.append(DICT_NAME.replace('.', '_')).append("=").append(getData(request).toString()).append(";");
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#exportAll(javax.servlet.http.HttpServletRequest)
     */
    public String exportAll(HttpServletRequest request) throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(new CmsGwtActionElement(null, request, null).export());
        sb.append(export(request));
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#getData(javax.servlet.http.HttpServletRequest)
     */
    // TODO: which parameter should be set from request? which can be set here?
    public JSONObject getData(HttpServletRequest request) {

        JSONObject keys = new JSONObject();
        // view, widget or editor dialogmode

        GalleryMode dialogMode = getDialogMode(request);
        switch (dialogMode) {

            case editor:
            case view:
            case widget:
                try {
                    keys.put(ReqParam.dialogmode.name(), dialogMode.name());
                    keys.put(ReqParam.gallerypath.name(), getGalleryPath(request));
                    keys.put(
                        ReqParam.gallerytabid.name(),
                        I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name());
                    keys.put(ReqParam.tabs.name(), GalleryConfiguration.TABS_DEFAULT.getConfig());
                    keys.put(ReqParam.types.name(), getTypesFromRequest(request));
                } catch (Throwable e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    try {
                        keys.put("error", e.getLocalizedMessage());
                    } catch (JSONException e1) {
                        // ignore, should never happen
                        LOG.error(e1.getLocalizedMessage(), e1);
                    }
                }
                break;
            case ade:
                try {

                    keys.put(ReqParam.dialogmode.name(), dialogMode.name());
                    keys.put(ReqParam.gallerypath.name(), "");
                    keys.put(
                        ReqParam.gallerytabid.name(),
                        I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name());
                    keys.put(ReqParam.tabs.name(), GalleryConfiguration.TABS_ADE.getConfig());
                    keys.put(ReqParam.types.name(), getTypesForContainerpage(request));

                } catch (Throwable e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    try {
                        keys.put("error", e.getLocalizedMessage());
                    } catch (JSONException e1) {
                        // ignore, should never happen
                        LOG.error(e1.getLocalizedMessage(), e1);
                    }
                }
                break;
            case sitemap:
                try {

                    keys.put(ReqParam.dialogmode.name(), dialogMode.name());
                    keys.put(ReqParam.gallerypath.name(), "");
                    keys.put(
                        ReqParam.gallerytabid.name(),
                        I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_vfstree.name());
                    keys.put(ReqParam.tabs.name(), GalleryConfiguration.TABS_SITEMAP.getConfig());
                    keys.put(ReqParam.types.name(), getTypesForSitemap(request));

                } catch (Throwable e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    try {
                        keys.put("error", e.getLocalizedMessage());
                    } catch (JSONException e1) {
                        // ignore, should never happen
                        LOG.error(e1.getLocalizedMessage(), e1);
                    }
                }
                break;
            default:
                break;
        }
        return keys;
    }

    /**
     * Returns the available resource types for this gallery dialog.<p>
     * 
     * @param request the current request to get the the parameter
     * 
     * @return the comma separated resource types
     */
    private GalleryMode getDialogMode(HttpServletRequest request) {

        // TODO: this is all crap, change it soon
        String temp = (CmsStringUtil.isNotEmptyOrWhitespaceOnly(request.getParameter(ReqParam.dialogmode.name()))
        ? request.getParameter(ReqParam.dialogmode.name())
        : (String)request.getAttribute(ReqParam.dialogmode.name()));
        GalleryMode mode = null;
        try {
            mode = GalleryMode.valueOf(temp);
        } catch (Exception e) {
            mode = GalleryMode.ade;
        }
        return mode;

    }

    /**
     * Returns the path to the gallery to open.<p>
     * 
     * @param request the current request to get the the parameter
     * 
     * @return the path to the gallery
     */
    private String getGalleryPath(HttpServletRequest request) {

        return request.getParameter(ReqParam.gallerypath.name());
    }

    /**
     * Returns the available resource types for this gallery dialog from request parameters.<p>
     * 
     * Use for gallery mode editor, view and widget.<p> 
     * 
     * @param request the current request to get the the parameter
     * 
     * @return the comma separated resource types
     */
    private String getTypesFromRequest(HttpServletRequest request) {

        return request.getParameter(ReqParam.types.name());
    }

    /**
     * Returns the available resource types for this gallery dialog within the container-page editor.<p>
     * 
     * Use for gallery mode ade.<p> 
     * 
     * @param request the current request
     * 
     * @return the comma separated resource types
     */
    private String getTypesForContainerpage(HttpServletRequest request) {

        //TODO: implement
        return "";
    }

    /**
     * Returns the available resource types for this gallery dialog within the sitemap editor.<p>
     * 
     * Use for gallery mode ade.<p> 
     * 
     * @param request the current request
     * 
     * @return the comma separated resource types
     */
    private String getTypesForSitemap(HttpServletRequest request) {

        //TODO: implement
        return "";
    }
}