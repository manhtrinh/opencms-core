package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsWorkplaceDefault.java,v $
 * Date   : $Date: 2000/10/25 08:14:28 $
 * Version: $Revision: 1.33 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

import javax.servlet.http.*;

/**
 * Common template class for displaying OpenCms workplace screens.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * <P>
 * Most special workplace classes may extend this class.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.33 $ $Date: 2000/10/25 08:14:28 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsWorkplaceDefault extends CmsXmlTemplate implements I_CmsWpConstants {
	
	/** URL of the pics folder in the webserver's docroot */
	private String m_picsurl = null;
	
	/** URL of the help folder */
	private String m_helpfolder = null;

	/** Reference to the config file */
	private CmsXmlWpConfigFile m_configFile = null;    

	/** Constant for the current language
	 * HACK: replace this by the corresponding value from the user object
	 */
	private final static String C_CURRENT_LANGUAGE = "de";
	
	/**
 * Sorts two vectors using bubblesort. This is a quick hack to display templates sorted by title instead of
 * by name in the template dropdown, because it is the title that is shown in the dropdown.
 * Creation date: (10/24/00 13:55:12)
 * @param names The vector to sort
 * @param data Vector with data that accompanies names.
 */
public void bubblesort(Vector names, Vector data)
{
	for (int i = 0; i < names.size() - 1; i++)
	{
		int len = names.size() - i - 1;
		for (int j = 0; j < len; j++)
		{
			String a = (String) names.elementAt(j);
			String b = (String) names.elementAt(j + 1);
			if (a.toLowerCase().compareTo(b.toLowerCase()) > 0)
			{
				names.setElementAt(a, j + 1);
				names.setElementAt(b, j);
				a = (String) data.elementAt(j);
				data.setElementAt(data.elementAt(j + 1), j);
				data.setElementAt(a, j + 1);
			}
		}
	}
}
	/**
	 * Checks a Java System property for containing the given value
	 * @param propertyName Name of the property
	 * @param value Value that should be checked
	 * @return <code>true</code> if the property contains the value, <code>false</code> otherwise
	 */
	protected boolean checkJavaProperty(String propertyName, String value) {
		boolean result = false;
		String prop = null;
		try {
			prop = System.getProperty(propertyName);
			if(prop != null && prop.equals(value)) {
				result = true;
			}     
			return result;
		} catch(Exception e) {
			return false;
		}
	}
	/**
	 * User method to generate an URL for the commom template pics folder.
	 * <P>
	 * All pictures should reside in the docroot of the webserver for
	 * performance reasons. This folder can be mounted into the OpenCms system to 
	 * make it accessible for the OpenCms explorer.
	 * <P>
	 * The path to the docroot can be set in the workplace ini.
	 * <P>
	 * In any workplace template file, this method can be invoked by
	 * <code>&lt;METHOD name="picsUrl"&gt;<em>PictureName</em>&lt;/METHOD&gt;</code>.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param tagcontent Unused in this special case of a user method. Can be ignored.
	 * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
	 * @param userObj Hashtable with parameters <em>(not used here)</em>.
	 * @return String with the pics URL.
	 * @exception CmsException
	 */    
	public Object commonPicsUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
			throws CmsException {
		if(m_picsurl == null) {            
			CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
			m_picsurl = configFile.getCommonPictureUrl();
		}
		return m_picsurl + tagcontent;
	}
	/**
	 * Used by workplace icons to decide whether the icon should 
	 * be activated or not. Icons will use this method if the attribute <code>method="doNotShow"</code>
	 * is defined in the <code>&lt;ICON&gt;</code> tag.
	 * <P>
	 * This method always returns <code>false</code> thus icons controlled by
	 * this method will never be activated.
	 * 
	 * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
	 * @param lang reference to the currently valid language file <em>(not used here)</em>.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return <code>false</code>.
	 */
	public Boolean doNotShow(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
		return new Boolean(false);
	}
	/**
	 * Gets a reference to the default config file.
	 * The path to this file ist stored in <code>C_WORKPLACE_INI</code>
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @return Reference to the config file.
	 * @exception CmsException
	 */
	public CmsXmlWpConfigFile getConfigFile(CmsObject cms) throws CmsException {
		//if(m_configFile == null) {
			m_configFile = new CmsXmlWpConfigFile(cms);
		//}
		return m_configFile;
	}
	/**
	 * Help method used to fill the vectors returned to 
	 * <code>CmsSelectBox</code> with constant values.
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param content String array containing the elements to be set.
	 * @param lang reference to the currently valid language file
	 */
	protected void getConstantSelectEntries(Vector names, Vector values, String[] contents, CmsXmlLanguageFile lang) 
			throws CmsException {
		for(int i=0; i<contents.length; i++) {
			String value = contents[i];
			values.addElement(value);
			if(lang.hasLanguageValue("select." + value)) {   
				names.addElement(lang.getLanguageValue("select." + value));
			} else {
				names.addElement(value);
			}
		}
	}
	/** Gets all fonts available in the workplace screens.
	 * <P>
	 * The given vectors <code>names</code> and <code>values</code> will 
	 * be filled with the appropriate information to be used for building
	 * a select box.
	 * <P>
	 * Used to build font select boxes in editors.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param lang reference to the currently valid language file
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return Index representing the user's current workplace view in the vectors.
	 * @exception CmsException
	 */
	public Integer getFontSizes(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
			throws CmsException {
		getConstantSelectEntries(names, values, C_SELECTBOX_FONTSIZES, lang);
		return new Integer(0);
	}
	/** Gets all fonts available in the workplace screens.
	 * <P>
	 * The given vectors <code>names</code> and <code>values</code> will 
	 * be filled with the appropriate information to be used for building
	 * a select box.
	 * <P>
	 * Used to build font select boxes in editors.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param lang reference to the currently valid language file
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return Index representing the user's current workplace view in the vectors.
	 * @exception CmsException
	 */
	public Integer getFontStyles(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
			throws CmsException {
		getConstantSelectEntries(names, values, C_SELECTBOX_FONTSTYLES, lang);
		return new Integer(0);
	}
	/**
	 * Gets all fonts available in the workplace screens.
	 * <P>
	 * The given vectors <code>names</code> and <code>values</code> will 
	 * be filled with the appropriate information to be used for building
	 * a select box.
	 * <P>
	 * Used to build font select boxes in editors.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param lang reference to the currently valid language file
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return Index representing the user's current workplace view in the vectors.
	 * @exception CmsException
	 */
	public Integer getFonts(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
			throws CmsException {
		getConstantSelectEntries(names, values, C_SELECTBOX_FONTS, lang);
		return new Integer(0);
	}
	/**
	 * Gets the key that should be used to cache the results of
	 * this template class. 
	 * 
	 * @param cms CmsObject Object for accessing system resources
	 * @param templateFile Filename of the template file 
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 * @return key that can be used for caching
	 */
	public Object getKey(CmsObject cms, String templateFile, Hashtable parameters, String templateSelector) {
		//Vector v = new Vector();
		CmsRequestContext reqContext = cms.getRequestContext();
		
		//v.addElement(templateFile);
		//v.addElement(parameters);
		//v.addElement(templateSelector);
		//return v;
		String result = "" + reqContext.currentProject().getId() + ":" + reqContext.currentUser().getName() + templateFile;
		Enumeration keys = parameters.keys();
		
		// select the right language to use
		String currentLanguage=null;
		Hashtable startSettings=null;
		startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);                    
 
		if (startSettings != null) {
			currentLanguage = (String)startSettings.get(C_START_LANGUAGE);  
		} else {        
			currentLanguage = C_DEFAULT_LANGUAGE;
		}
			 
		while(keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			result = result + key + parameters.get(key);            
		}
		result = result + templateSelector+currentLanguage;
		return result;        
	}
	/**
	 * Get the currently valid <code>lasturl</code> parameter that can be 
	 * used for redirecting to the previous workplace screen.
	 * @param cms Cms object for accessing system resources.
	 * @param parameters User parameters.
	 * @return <code>lasturl</code> parameter.
	 */
	protected String getLastUrl(CmsObject cms, Hashtable parameters) {
		I_CmsSession session = cms.getRequestContext().getSession(true);
		String lasturl = (String)parameters.get("lasturl");
		
		// Lasturl parameter will be taken either from the parameter hashtable
		// (if exists) or from the session storage.
		// If neccessary, session storage will be updated.
		StringBuffer encLasturl = new StringBuffer();
		boolean notfirst = false;
		if(lasturl != null) {
			// Fine. A lasturl parameter was found in session or parameter hashtable.
			// Check, if the URL parameters of the last url have to be encoded.
			int asteriskIdx = lasturl.indexOf("?");
			if(asteriskIdx > -1 && (asteriskIdx < (lasturl.length()-1))) {
				// In fact, there are URL parameters
				encLasturl.append(lasturl.substring(0, asteriskIdx + 1));       
				String queryString = lasturl.substring(asteriskIdx + 1);
				StringTokenizer st = new StringTokenizer(queryString, "&");
				while(st.hasMoreTokens()) {
					// Loop through all URL parameters
					String currToken = st.nextToken();
					if(currToken != null && !"".equals(currToken)) {
						// Look for the "=" character to divide parameter name and value
						int idx = currToken.indexOf("=");
						if(notfirst) {
							encLasturl.append("&");
						} else {
							notfirst = true;
						}
						if(idx > -1) {
							// A parameter name/value pair was found.
							// Encode the parameter value and write back!
							String key = currToken.substring(0,idx);
							String value = (idx < (currToken.length()-1))?currToken.substring(idx+1):"";
							encLasturl.append(key);
							encLasturl.append("=");
							encLasturl.append(Encoder.escape(value));
						} else {
							// Something strange happened.
							// Maybe a parameter without "=" ?
							// Write back without encoding!
							encLasturl.append(currToken);
						}
					}                    
				}
				lasturl = encLasturl.toString();
			}

			session.putValue("lasturl", lasturl);                    
		} else {
			lasturl = (String)session.getValue("lasturl");
		}
		return lasturl;
	}
	/**
	 * Reads in the template file and starts the XML parser for the expected
	 * content type <class>CmsXmlWpTemplateFile</code>
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param templateFile Filename of the template file.
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 */
	public CmsXmlTemplateFile getOwnTemplateFile(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);       
		return xmlTemplateDocument;
	}
	/**
	 * User method to generate an URL for a help file.
	 * The system help file path and the currently selected language will
	 * be considered.
	 * <P>
	 * The path to the help file folder can be set in the workplace ini.
	 * <P>
	 * In any workplace template file, this method can be invoked by
	 * <code>&lt;METHOD name="helpUrl"&gt;<em>HelpFileName</em>&lt;/METHOD&gt;</code>.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param tagcontent Unused in this special case of a user method. Can be ignored.
	 * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
	 * @param userObj Hashtable with parameters <em>(not used here)</em>.
	 * @return String with the pics URL.
	 * @exception CmsException
	 */    
	public Object helpUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
			throws CmsException {        
		if(m_helpfolder == null) { 
			m_helpfolder = getConfigFile(cms).getHelpPath();
		}
		CmsRequestContext reqCont = cms.getRequestContext();
		String servletPath = ((HttpServletRequest)reqCont.getRequest().getOriginalRequest()).getServletPath();
		String currentLanguage = C_CURRENT_LANGUAGE.toLowerCase();
		char separator = m_picsurl.charAt(m_picsurl.length()-1);
		return servletPath + m_helpfolder + currentLanguage + separator + tagcontent;
	}
	 /**
	 * Checks if the current user is <strong>administrator</strong>.
	 * <P>
	 * This method is used by workplace icons to decide whether the icon should 
	 * be activated or not. Icons will use this method if the attribute <code>method="isAdministrator"</code>
	 * is defined in the <code>&lt;ICON&gt;</code> tag.
	 * 
	 * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
	 * @param lang reference to the currently valid language file <em>(not used here)</em>.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return <code>true</code> if the current project is the online project, <code>false</code> otherwise.
	 * @exception CmsException if there were errors while accessing project data.
	 */
	public Boolean isAdmin(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) 
			throws CmsException {           
		CmsRequestContext reqCont = cms.getRequestContext();
		return new Boolean(reqCont.isAdmin());
	}
	/**
	 * Indicates if the results of this class are cacheable.
	 * 
	 * @param cms CmsObject Object for accessing system resources
	 * @param templateFile Filename of the template file 
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
	 */
	public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
		return false;
	}
	/**
	 * Checks if the current project is <STRONG>not</STRONG> the "Online" project.
	 * <P>
	 * This method is used by workplace icons to decide whether the icon should 
	 * be activated or not. Icons will use this method if the attribute <code>method="isNotOnlineProject"</code>
	 * is defined in the <code>&lt;ICON&gt;</code> tag.
	 * 
	 * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
	 * @param lang reference to the currently valid language file <em>(not used here)</em>.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return <code>true</code> if the current project is the online project, <code>false</code> otherwise.
	 * @exception CmsException if there were errors while accessing project data.
	 */
	public Boolean isNotOnlineProject(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) 
			throws CmsException {           
		CmsRequestContext reqCont = cms.getRequestContext();
		return new Boolean(!reqCont.currentProject().equals(cms.onlineProject()));
	}
	 /**
	 * Checks if the current user is <STRONG>Project manager</STRONG>.
	 * <P>
	 * This method is used by workplace icons to decide whether the icon should 
	 * be activated or not. Icons will use this method if the attribute <code>method="isProjectManager"</code>
	 * is defined in the <code>&lt;ICON&gt;</code> tag.
	 * 
	 * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
	 * @param lang reference to the currently valid language file <em>(not used here)</em>.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return <code>true</code> if the current project is the online project, <code>false</code> otherwise.
	 * @exception CmsException if there were errors while accessing project data.
	 */
	public Boolean isProjectManager(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) 
			throws CmsException {           
		CmsRequestContext reqCont = cms.getRequestContext();
		return new Boolean((reqCont.isAdmin() || reqCont.isProjectManager()));
	}
	/**
	 * User method to generate an URL for the system pics folder.
	 * <P>
	 * All pictures should reside in the docroot of the webserver for
	 * performance reasons. This folder can be mounted into the OpenCms system to 
	 * make it accessible for the OpenCms explorer.
	 * <P>
	 * The path to the docroot can be set in the workplace ini.
	 * <P>
	 * In any workplace template file, this method can be invoked by
	 * <code>&lt;METHOD name="picsUrl"&gt;<em>PictureName</em>&lt;/METHOD&gt;</code>.
	 * <P>
	 * <b>Warning:</b> Using this method, only workplace pictures, usually residing
	 * in the <code>pics/system/</code> folder, can be accessed. In any workplace class 
	 * template pictures can be accessed via <code>commonPicsUrl</code>.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param tagcontent Unused in this special case of a user method. Can be ignored.
	 * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
	 * @param userObj Hashtable with parameters <em>(not used here)</em>.
	 * @return String with the pics URL.
	 * @exception CmsException
	 * @see #commonPicsUrl
	 */    
	public Object picsUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
			throws CmsException {
		if(m_picsurl == null) {            
			CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
			m_picsurl = configFile.getWpPictureUrl();
		}
		return m_picsurl + tagcontent;
	}
	/**
	 * Starts the processing of the given template file by calling the
	 * <code>getProcessedTemplateContent()</code> method of the content defintition
	 * of the corresponding content type.
	 * <P>
	 * Any exceptions thrown while processing the template will be caught,
	 * printed and and thrown again.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param xmlTemplateDocument XML parsed document of the content type "XML template file" or
	 * any derived content type.
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 * @return Content of the template and all subtemplates.
	 * @exception CmsException 
	 */
	protected byte[] startProcessing(CmsObject cms, CmsXmlTemplateFile xmlTemplateDocument, String elementName, Hashtable parameters, String templateSelector) throws CmsException {     
		String lasturl = getLastUrl(cms, parameters);

		// Since we are in the workplace, no browser caching is allowed here.
		// Set all caching information to "no-cache".
		// Don't bother about the internal caching here! Workplace users should be forced
		// to reload the workplace pages at every request.

		//HTTP 1.1
		cms.getRequestContext().getResponse().setHeader("Cache-Control", "no-cache");
		//HTTP 1.0
		cms.getRequestContext().getResponse().setHeader("Pragma","no-cache");
		((CmsXmlWpTemplateFile)xmlTemplateDocument).setData("lasturl", lasturl);        
		return super.startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
	}
	/**
	 * User method to get the name of the user.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param tagcontent Unused in this special case of a user method. Can be ignored.
	 * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
	 * @param userObj Hashtable with parameters <em>(not used here)</em>.
	 * @return String with the pics URL.
	 * @exception CmsException
	 */    
	public Object userName(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
		throws CmsException {
		return Utils.getFullName(cms.getRequestContext().currentUser());
	}
}
