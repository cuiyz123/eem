// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   XMLHelper.java

package com.metarnet.eomeem.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.List;

public final class XMLHelper
{
	public static class ErrorLogger
		implements ErrorHandler
	{

		private String file;
		private List errors;

		public void error(SAXParseException error)
		{
			XMLHelper.log.error((new StringBuilder("Error parsing XML: ")).append(file).append('(').append(error.getLineNumber()).append(") ").append(error.getMessage()).toString());
			errors.add(error);
		}

		public void fatalError(SAXParseException error)
		{
			error(error);
		}

		public void warning(SAXParseException warn)
		{
			XMLHelper.log.warn((new StringBuilder("Warning parsing XML: ")).append(file).append('(').append(warn.getLineNumber()).append(") ").append(warn.getMessage()).toString());
		}

		ErrorLogger(String file, List errors)
		{
			this.file = file;
			this.errors = errors;
		}
	}


	private static final Log log = LogFactory.getLog("com/wayout/webframe/util/XMLHelper");

	public static SAXReader createSAXReader(String file, List errorsList)
	{
		SAXReader reader = new SAXReader();
		reader.setErrorHandler(new ErrorLogger(file, errorsList));
		reader.setMergeAdjacentText(true);
		reader.setValidation(false);

		return reader;
	}

	public static DOMReader createDOMReader()
	{
		DOMReader reader = new DOMReader();
		return reader;
	}

	private XMLHelper()
	{
	}


}
