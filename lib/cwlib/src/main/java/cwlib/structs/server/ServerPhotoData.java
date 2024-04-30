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

public class ServerPhotoData
{
    public int id;
    public String author;
    public String small, medium, large;
    public String plan;
    public int slotID;

    public static ServerPhotoData[] parse(String xml)
    {
        return parse(xml.getBytes(StandardCharsets.UTF_8));
    }

    public static ServerPhotoData[] parse(byte[] xml)
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
                case "photo":
                    elements = new Element[] { doc.getDocumentElement() };
                    break;
                case "photos":
                {
                    NodeList list = doc.getElementsByTagName("photo");
                    elements = IntStream.range(0, list.getLength())
                        .mapToObj(list::item)
                        .collect(Collectors.toList())
                        .toArray(Element[]::new);
                    break;
                }
                default:
                    return new ServerPhotoData[0];
            }

            ServerPhotoData[] descriptors = new ServerPhotoData[elements.length];
            int i = 0;
            for (Element element : elements)
            {
                ServerPhotoData descriptor = new ServerPhotoData();


                NodeList children = null;

                if ((children = element.getElementsByTagName("id")).getLength() != 0)
                    descriptor.id = Integer.parseInt(children.item(0).getTextContent());
                if ((children = element.getElementsByTagName("author")).getLength() != 0)
                    descriptor.author = children.item(0).getTextContent();
                if ((children = element.getElementsByTagName("small")).getLength() != 0)
                    descriptor.small = children.item(0).getTextContent();
                if ((children = element.getElementsByTagName("medium")).getLength() != 0)
                    descriptor.medium = children.item(0).getTextContent();
                if ((children = element.getElementsByTagName("large")).getLength() != 0)
                    descriptor.large = children.item(0).getTextContent();
                if ((children = element.getElementsByTagName("plan")).getLength() != 0)
                    descriptor.plan = children.item(0).getTextContent();

                if ((children = element.getElementsByTagName("slot")).getLength() != 0)
                {

                    Element location = (Element) children.item(0);
                    if ((children = location.getElementsByTagName("id")).getLength() != 0)
                        descriptor.slotID =
                            Integer.parseInt(children.item(0).getTextContent());


                }

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