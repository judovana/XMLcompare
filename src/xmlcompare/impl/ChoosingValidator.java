/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;

/**
 *
 * @author jvanek
 */
public class ChoosingValidator {

    final File xml;
    final File schema;

    public ChoosingValidator(File xml, File schema) {
        this.xml = xml;
        this.schema = schema;
    }

    public boolean validate() {
        try {
            // build an XSD-aware SchemaFactory
            final SchemaFactory schemaFactory;
            if (schema.getName().toLowerCase().endsWith(".xsd")) {
                schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            } else if (schema.getName().toLowerCase().endsWith(".dtd")) {
                schemaFactory = SchemaFactory.newInstance(XMLConstants.XML_DTD_NS_URI);
            } else if (schema.getName().toLowerCase().endsWith(".rng")) {
                schemaFactory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
            } else {
                throw new IllegalArgumentException("unknown schema type (non dtd, xsd, rng)");
            }
            // hook up mindless org.xml.sax.ErrorHandler implementation.
            //  schemaFactory.setErrorHandler( new JNLPErrorHandler() );
            // get the custom xsd schema describing the required format for the jnlp XML files.
            final Schema schemaDef = schemaFactory.newSchema(schema);
            // Create a Validator capable of validating JNLP files according to to the custom schema.
            final Validator validator = schemaDef.newValidator();
            // Create a generic XML parser.
            final DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // parse the JNLP file on the command line purely as XML and get a DOM tree representation.
            final Document document = parser.parse(xml);
            // Validate the JNLP tree against the stricter XSD schema
            validator.validate(new DOMSource(document));
        } catch (Exception e) {
            System.out.flush();
            System.err.flush();
            e.printStackTrace();
            System.err.flush();
            System.out.flush();
            return false;
        }
        return true;

    }

}
