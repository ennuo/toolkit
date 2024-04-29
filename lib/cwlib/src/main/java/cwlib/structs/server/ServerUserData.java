package cwlib.structs.server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerUserData
{
    public int photosByMeCount;
    public int photosWithMeCount;
    public int commentCount;
    public int reviewCount;

    public static ServerUserData[] parse(String xml)
    {
        return parse(xml.getBytes(StandardCharsets.UTF_8));
    }

    public static ServerUserData[] parse(byte[] xml)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xml));

            doc.getDocumentElement().normalize();

            Element[] elements = null;
            switch (doc.getDocumentElement().getNodeName())
            {
                case "user":
                    elements = new Element[] { doc.getDocumentElement() };
                    break;
                case "users":
                {
                    NodeList list = doc.getElementsByTagName("photo");
                    elements = IntStream.range(0, list.getLength())
                        .mapToObj(list::item)
                        .collect(Collectors.toList())
                        .toArray(Element[]::new);
                    break;
                }
                default:
                    return new ServerUserData[0];
            }

            ServerUserData[] descriptors = new ServerUserData[elements.length];
            int i = 0;
            for (Element element : elements)
            {
                ServerUserData descriptor = new ServerUserData();


                NodeList children = null;

                if ((children = element.getElementsByTagName("reviewCount")).getLength() != 0)
                    descriptor.reviewCount =
                        Integer.parseInt(children.item(0).getTextContent());
                if ((children = element.getElementsByTagName("commentCount")).getLength() != 0)
                    descriptor.commentCount =
                        Integer.parseInt(children.item(0).getTextContent());
                if ((children = element.getElementsByTagName("photosByMeCount")).getLength() != 0)
                    descriptor.photosByMeCount =
                        Integer.parseInt(children.item(0).getTextContent());
                if ((children = element.getElementsByTagName("photosWithMeCount")).getLength() != 0)
                    descriptor.photosWithMeCount =
                        Integer.parseInt(children.item(0).getTextContent());

                descriptors[i++] = descriptor;
            }

            return descriptors;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }


}
