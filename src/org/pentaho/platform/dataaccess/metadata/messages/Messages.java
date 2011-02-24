/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved.
 * 
 * Created Jan, 2011
*/
package org.pentaho.platform.dataaccess.metadata.messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.messages.MessageUtil;


public class Messages {
  private static final String BUNDLE_NAME = "org.pentaho.platform.dataaccess.metadata.messages.messages"; //$NON-NLS-1$

  private static final Map locales = Collections.synchronizedMap(new HashMap());

  private static ResourceBundle getBundle() {
    Locale locale = LocaleHelper.getLocale();
    ResourceBundle bundle = (ResourceBundle) Messages.locales.get(locale);
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(Messages.BUNDLE_NAME, locale);
      Messages.locales.put(locale, bundle);
    }
    return bundle;
  }

  public static String getString(final String key) {
    try {
      return Messages.getBundle().getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  public static String getString(final String key, final String param1) {
    return MessageUtil.getString(Messages.getBundle(), key, param1);
  }

  public static String getString(final String key, final String param1, final String param2) {
    return MessageUtil.getString(Messages.getBundle(), key, param1, param2);
  }

  public static String getString(final String key, final String param1, final String param2, final String param3) {
    return MessageUtil.getString(Messages.getBundle(), key, param1, param2, param3);
  }

  public static String getString(final String key, final String param1, final String param2, final String param3, final String param4) {
    return MessageUtil.getString(Messages.getBundle(), key, param1, param2, param3, param4);
  }

  public static String getErrorString(final String key) {
    return MessageUtil.formatErrorMessage(key, Messages.getString(key));
  }

  public static String getErrorString(final String key, final String param1) {
    return MessageUtil.getErrorString(Messages.getBundle(), key, param1);
  }

  public static String getErrorString(final String key, final String param1, final String param2) {
    return MessageUtil.getErrorString(Messages.getBundle(), key, param1, param2);
  }

  public static String getErrorString(final String key, final String param1, final String param2, final String param3) {
    return MessageUtil.getErrorString(Messages.getBundle(), key, param1, param2, param3);
  }

}