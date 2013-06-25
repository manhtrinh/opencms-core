/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.widgets;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.CmsEditorDisplayOptions;
import org.opencms.workplace.editors.CmsWorkplaceEditorConfiguration;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

/**
 * Provides a widget that creates a rich input field using the matching component, for use on a widget dialog.<p>
 * 
 * The matching component is determined by checking the installed editors for the best matching component to use.<p>
 * 
 * @since 6.0.1 
 */
public class CmsHtmlWidget extends A_CmsHtmlWidget implements I_CmsADEWidget {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlWidget.class);

    /** The editor widget to use depending on the current users settings, current browser and installed editors. */
    private I_CmsWidget m_editorWidget;

    /**
     * Creates a new html editing widget.<p>
     */
    public CmsHtmlWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new html editing widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsHtmlWidget(CmsHtmlWidgetOption configuration) {

        super(configuration);
    }

    /**
     * Creates a new html editing widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsHtmlWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        return getJSONConfiguration(cms, resource).toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        // not needed for internal widget
        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.wide;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getEditorWidget(cms, widgetDialog).getDialogIncludes(cms, widgetDialog);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getEditorWidget(cms, widgetDialog).getDialogInitCall(cms, widgetDialog);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getEditorWidget(cms, widgetDialog).getDialogInitMethod(cms, widgetDialog);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        return getEditorWidget(cms, widgetDialog).getDialogWidget(cms, widgetDialog, param);
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    public String getInitCall() {

        // not needed for internal widget
        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        // not needed for internal widget
        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return CmsHtmlWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    public boolean isInternal() {

        return true;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsHtmlWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            String val = CmsEncoder.decode(values[0], CmsEncoder.ENCODING_UTF_8);
            param.setStringValue(cms, val);
        }
    }

    /**
     * Returns the WYSIWYG editor configuration as a JSON object.<p>
     * 
     * @param cms the OpenCms context
     * @param resource the edited resource
     * 
     * @return the configuration
     */
    protected JSONObject getJSONConfiguration(CmsObject cms, CmsResource resource) {

        JSONObject result = new JSONObject();

        CmsHtmlWidgetOption widgetOptions = getHtmlWidgetOption();
        CmsEditorDisplayOptions options = OpenCms.getWorkplaceManager().getEditorDisplayOptions();
        Properties displayOptions = options.getDisplayOptions(cms);
        try {
            if (options.showElement("gallery.enhancedoptions", displayOptions)) {
                result.put("cmsGalleryEnhancedOptions", true);
            }
            if (options.showElement("gallery.usethickbox", displayOptions)) {
                result.put("cmsGalleryUseThickbox", true);
            }
            result.put("fullpage", widgetOptions.isFullPage());
            List<String> toolbarItems = widgetOptions.getButtonBarShownItems();
            result.put("toolbar_items", toolbarItems);
            result.put("language", OpenCms.getWorkplaceManager().getWorkplaceLocale(cms).getLanguage());
            String editorHeight = widgetOptions.getEditorHeight();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(editorHeight)) {
                editorHeight = editorHeight.replaceAll("px", "");
                result.put("height", editorHeight);
            }
            // set CSS style sheet for current editor widget if configured
            boolean cssConfigured = false;
            String cssPath = "";
            if (widgetOptions.useCss()) {
                cssPath = widgetOptions.getCssPath();
                // set the CSS path to null (the created configuration String passed to JS will not include this path then)
                widgetOptions.setCssPath(null);
                cssConfigured = true;
            } else if (OpenCms.getWorkplaceManager().getEditorCssHandlers().size() > 0) {
                Iterator<I_CmsEditorCssHandler> i = OpenCms.getWorkplaceManager().getEditorCssHandlers().iterator();
                try {
                    String editedResourceSitePath = resource == null ? null : cms.getSitePath(resource);
                    while (i.hasNext()) {
                        I_CmsEditorCssHandler handler = i.next();
                        if (handler.matches(cms, editedResourceSitePath)) {
                            cssPath = handler.getUriStyleSheet(cms, editedResourceSitePath);
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(cssPath)) {
                                cssConfigured = true;
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    // ignore, CSS could not be set
                }
            }
            if (cssConfigured) {
                result.put("content_css", OpenCms.getLinkManager().substituteLink(cms, cssPath));
            }

            if (widgetOptions.showStylesFormat()) {
                try {
                    CmsFile file = cms.readFile(widgetOptions.getStylesFormatPath());
                    String characterEncoding = OpenCms.getSystemInfo().getDefaultEncoding();
                    result.put("style_formats", new String(file.getContents(), characterEncoding));
                } catch (CmsException cmsException) {
                    LOG.error("Can not open file:" + widgetOptions.getStylesFormatPath(), cmsException);
                } catch (UnsupportedEncodingException ex) {
                    LOG.error(ex);
                }
            }
            String formatSelectOptions = widgetOptions.getFormatSelectOptions();
            if (!CmsStringUtil.isEmpty(formatSelectOptions)
                && !widgetOptions.isButtonHidden(CmsHtmlWidgetOption.OPTION_FORMATSELECT)) {
                formatSelectOptions = StringUtils.replace(formatSelectOptions, ";", ",");
                result.put("block_formats", formatSelectOptions);
            }
            CmsWorkplaceEditorConfiguration editorConfig = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getEditorConfiguration(
                "tinymce");
            Boolean pasteText = Boolean.valueOf(editorConfig.getParameters().get("paste_text"));
            JSONObject directOptions = new JSONObject();
            directOptions.put("paste_text_sticky_default", pasteText);
            directOptions.put("paste_text_sticky", Boolean.TRUE);
            result.put("tinyMceOptions", directOptions);

        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * Returns the editor widget to use depending on the current users settings, current browser and installed editors.<p>
     * 
     * @param cms the current CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @return the editor widget to use depending on the current users settings, current browser and installed editors
     */
    private I_CmsWidget getEditorWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        if (m_editorWidget == null) {
            // get HTML widget to use from editor manager
            String widgetClassName = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getWidgetEditor(
                cms.getRequestContext(),
                widgetDialog.getUserAgent());
            boolean foundWidget = true;
            if (CmsStringUtil.isEmpty(widgetClassName)) {
                // no installed widget found, use default text area to edit HTML value
                widgetClassName = CmsTextareaWidget.class.getName();
                foundWidget = false;
            }
            try {
                if (foundWidget) {
                    // get widget instance and set the widget configuration
                    Class<?> widgetClass = Class.forName(widgetClassName);
                    A_CmsHtmlWidget editorWidget = (A_CmsHtmlWidget)widgetClass.newInstance();
                    editorWidget.setHtmlWidgetOption(getHtmlWidgetOption());
                    m_editorWidget = editorWidget;
                } else {
                    // set the text area to display 15 rows for editing
                    Class<?> widgetClass = Class.forName(widgetClassName);
                    I_CmsWidget editorWidget = (I_CmsWidget)widgetClass.newInstance();
                    editorWidget.setConfiguration("15");
                    m_editorWidget = editorWidget;
                }
            } catch (Exception e) {
                // failed to create widget instance
                LOG.error(Messages.get().container(Messages.LOG_CREATE_HTMLWIDGET_INSTANCE_FAILED_1, widgetClassName).key());
            }

        }
        return m_editorWidget;
    }
}