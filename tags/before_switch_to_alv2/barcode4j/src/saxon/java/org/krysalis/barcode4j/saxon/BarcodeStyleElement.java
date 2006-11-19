/*
 * $Id: BarcodeStyleElement.java,v 1.1 2003-12-13 20:23:43 jmaerki Exp $
 * ============================================================================
 * The Krysalis Patchy Software License, Version 1.1_01
 * Copyright (c) 2003 Nicola Ken Barozzi.  All rights reserved.
 *
 * This Licence is compatible with the BSD licence as described and
 * approved by http://www.opensource.org/, and is based on the
 * Apache Software Licence Version 1.1.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed for project
 *        Krysalis (http://www.krysalis.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Krysalis" and "Nicola Ken Barozzi" and
 *    "Barcode4J" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact nicolaken@krysalis.org.
 *
 * 5. Products derived from this software may not be called "Krysalis"
 *    or "Barcode4J", nor may "Krysalis" appear in their name,
 *    without prior written permission of Nicola Ken Barozzi.
 *
 * 6. This software may contain voluntary contributions made by many
 *    individuals, who decided to donate the code to this project in
 *    respect of this licence, and was originally created by
 *    Jeremias Maerki <jeremias@maerki.org>.
 *
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE KRYSALIS PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
package org.krysalis.barcode4j.saxon;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.krysalis.barcode4j.BarcodeException;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.BarcodeUtil;
import org.krysalis.barcode4j.output.svg.SVGCanvasProvider;
import org.krysalis.barcode4j.tools.ConfigurationUtil;
import org.xml.sax.SAXException;

import com.icl.saxon.ContentEmitter;
import com.icl.saxon.Context;
import com.icl.saxon.DOMDriver;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.style.StyleElement;

/**
 * This represents the main barcode element.
 * 
 * @author Jeremias Maerki
 */
public class BarcodeStyleElement extends StyleElement {

    private Logger log = new org.apache.avalon.framework.logger.ConsoleLogger();

    private Expression message;


    /**
     * @see com.icl.saxon.style.StyleElement#prepareAttributes()
     */
    public void prepareAttributes() throws TransformerConfigurationException {
        // Get mandatory message attribute
        
        String msgAtt = attributeList.getValue("message");
        if (msgAtt == null) {
            reportAbsence("message");
        }
        message = makeAttributeValueTemplate(msgAtt);
    }


    /**
     * @see com.icl.saxon.style.StyleElement#process(com.icl.saxon.Context)
     */
    public void process(Context context) throws TransformerException {
        final Configuration cfg = ConfigurationUtil.buildConfiguration(this);

        try {
            //Acquire BarcodeGenerator
            final BarcodeGenerator gen = 
                    BarcodeUtil.getInstance().createBarcodeGenerator(cfg, log);
    
            //Setup Canvas
            final SVGCanvasProvider svg;
            if (cfg.getAttributeAsBoolean("useNamespace", true)) {
                svg = new SVGCanvasProvider(cfg.getAttribute("prefix", "svg"));
            } else {
                svg = new SVGCanvasProvider(false);
            }
            //Generate barcode
            gen.generateBarcode(svg, message.evaluateAsString(context));
            
            ContentEmitter ce = new ContentEmitter();
            ce.setEmitter(context.getOutputter().getEmitter());
            ce.setNamePool(NamePool.getDefaultNamePool());
            try {
                DOMDriver domdriver = new DOMDriver();
                domdriver.setStartNode(svg.getDOMFragment());
                domdriver.setContentHandler(ce);
                domdriver.parse();
            } catch (SAXException saxe) {
                throw new TransformerException(saxe);
            }
        } catch (BarcodeException be) {
            throw new TransformerException(be);
        }
    }
   

    /**
     * @see com.icl.saxon.style.StyleElement#isInstruction()
     */
    public boolean isInstruction() {
        return true;
    }

}