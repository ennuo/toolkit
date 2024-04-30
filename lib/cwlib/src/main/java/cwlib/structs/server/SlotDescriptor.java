package cwlib.structs.server;

import cwlib.types.data.SHA1;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SlotDescriptor
{
    public int id;
    public String name;
    public String description;
    public String root;
    public String icon;
    public int x, y;
    public SHA1[] resources;
    public String[] labels;
    public boolean locked;
    public boolean subLevel;
    public int shareable;
    public int background;
    public int minPlayers = 1, maxPlayers = 4;
    public boolean isAdventurePlanet = false;

    public static SlotDescriptor[] parse(String xml)
    {
        return parse(xml.getBytes(StandardCharsets.UTF_8));
    }

    public static SlotDescriptor[] parse(byte[] xml)
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
                case "slot":
                    elements = new Element[] { doc.getDocumentElement() };
                    break;
                case "slots":
                {
                    NodeList list = doc.getElementsByTagName("slot");
                    elements = IntStream.range(0, list.getLength())
                        .mapToObj(list::item)
                        .collect(Collectors.toList())
                        .toArray(Element[]::new);
                    break;
                }
                default:
                    return new SlotDescriptor[0];
            }

            SlotDescriptor[] descriptors = new SlotDescriptor[elements.length];
            int i = 0;
            for (Element element : elements)
            {
                SlotDescriptor descriptor = new SlotDescriptor();


                NodeList children = null;

                if ((children = element.getElementsByTagName("id")).getLength() != 0)
                    descriptor.id = Integer.parseInt(children.item(0).getTextContent());
                if ((children = element.getElementsByTagName("name")).getLength() != 0)
                    descriptor.name = children.item(0).getTextContent();
                if ((children = element.getElementsByTagName("description")).getLength() != 0)
                    descriptor.description = children.item(0).getTextContent();
                if ((children = element.getElementsByTagName("rootLevel")).getLength() != 0)
                    descriptor.root = children.item(0).getTextContent().toLowerCase();
                if ((children = element.getElementsByTagName("icon")).getLength() != 0)
                    descriptor.icon = children.item(0).getTextContent().toLowerCase();
                if ((children = element.getElementsByTagName("initiallyLocked")).getLength() != 0)
                    descriptor.locked =
                        children.item(0).getTextContent().equalsIgnoreCase("true");
                if ((children = element.getElementsByTagName("isSubLevel")).getLength() != 0)
                    descriptor.subLevel =
                        children.item(0).getTextContent().equalsIgnoreCase(
                            "true");
                if ((children = element.getElementsByTagName("background")).getLength() != 0)
                    descriptor.background =
                        Integer.parseInt(children.item(0).getTextContent());
                if ((children = element.getElementsByTagName("shareable")).getLength() != 0)
                    descriptor.shareable =
                        Integer.parseInt(children.item(0).getTextContent());
                if ((children = element.getElementsByTagName("authorLabels")).getLength() != 0)
                    descriptor.labels = children.item(0).getTextContent().split(",");
                if ((children = element.getElementsByTagName("minPlayers")).getLength() != 0)
                    descriptor.minPlayers =
                        Integer.parseInt(children.item(0).getTextContent());
                if ((children = element.getElementsByTagName("maxPlayers")).getLength() != 0)
                    descriptor.maxPlayers =
                        Integer.parseInt(children.item(0).getTextContent());
                if ((children = element.getElementsByTagName("isAdventurePlanet")).getLength() != 0)
                    descriptor.isAdventurePlanet =
                        children.item(0).getTextContent().equalsIgnoreCase("true");

                if ((children = element.getElementsByTagName("location")).getLength() != 0)
                {

                    Element location = (Element) children.item(0);
                    if ((children = location.getElementsByTagName("x")).getLength() != 0)
                        descriptor.x =
                            Integer.parseInt(children.item(0).getTextContent());
                    if ((children = location.getElementsByTagName("y")).getLength() != 0)
                        descriptor.y =
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
