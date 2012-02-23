/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsFindAttributeDefNamesResults;


/**
 * @author mchyzer
 *
 */
public class WsSampleFindAttributeDefNames implements WsSampleGenerated {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        findAttributeDefName(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
      findAttributeDefName(wsSampleGeneratedType);
    }

    /**
     *
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void findAttributeDefName(WsSampleGeneratedType wsSampleGeneratedType) {
        try {
            //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
            GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(GeneratedClientSettings.USER);
            auth.setPassword(GeneratedClientSettings.PASS);
            auth.setPreemptiveAuthentication(true);

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

            FindAttributeDefNames findAttributeDefNames = null;
            FindAttributeDefNamesResponse findAttributeDefNamesResponse = null;
            WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = null;

            findAttributeDefNames = FindAttributeDefNames.class.newInstance();

            //version, e.g. v1_3_000
            findAttributeDefNames.setClientVersion(GeneratedClientSettings.VERSION);
            findAttributeDefNames.setScope("test:%");

            findAttributeDefNamesResponse = stub.findAttributeDefNames(findAttributeDefNames);
            wsFindAttributeDefNamesResults = findAttributeDefNamesResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindAttributeDefNamesResults));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindAttributeDefNamesResults.getAttributeDefNameResults()[0]));
            
            if (!StringUtils.equals("T", 
                wsFindAttributeDefNamesResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
