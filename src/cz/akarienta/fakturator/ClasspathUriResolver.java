package cz.akarienta.fakturator;

import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * URI resolver to have xml config files in java package.
 *
 * @author akarienta
 */
public class ClasspathUriResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        Source source = null;
        InputStream inputStream = getClass().getResourceAsStream(href);
        if (inputStream != null) {
            source = new StreamSource(inputStream);
        }
        return source;
    }
}
