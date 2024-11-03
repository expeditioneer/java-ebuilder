package org.gentoo.java.ebuilder.parser;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.gentoo.java.ebuilder.maven.MavenParser;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@ApplicationScoped
public class XMLUtilities {

    private static final Logger LOG = Logger.getLogger(XMLUtilities.class);

    public InputStream createInputStream(Document document) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result target = new StreamResult(outputStream);
        Source source = new DOMSource(document);

        try {
            TransformerFactory.newInstance()
                    .newTransformer()
                    .transform(source, target);
        } catch (TransformerException e) {
            LOG.error("Transformer exception occurred", e);
            throw new RuntimeException(e);
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
