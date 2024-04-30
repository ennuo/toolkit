package cwlib.util;

import java.nio.charset.StandardCharsets;
import java.util.Stack;

public class XmlFormatter
{
    private final StringBuilder buffer;
    private int depth;
    private final Stack<String> tags;

    public XmlFormatter(int size)
    {
        buffer = new StringBuilder(size);
        depth = 0;
        tags = new Stack<>();
    }

    public void startTag(String name)
    {
        tabinate();

        depth++;

        buffer.append(String.format("<%s>\n", name));
        tags.push(name);
    }

    public void startTag(String name, String attributes)
    {
        if (attributes == null || attributes.isEmpty())
        {
            startTag(name);
            return;
        }

        tabinate();

        depth++;

        buffer.append(String.format("<%s %s>\n", name, attributes));
        tags.push(name);
    }

    public void endTag()
    {
        if (depth == 0) return;

        depth--;

        tabinate();

        String name = tags.pop();
        buffer.append(String.format("</%s>\n", name, name));
    }

    public void addTag(String name, Object value)
    {
        tabinate();

        if (value == null || (value instanceof String && ((String) value).isEmpty()))
        {
            buffer.append(String.format("<%s />\n", name));
            return;
        }

        buffer.append(String.format("<%s>%s</%s>\n", name, value, name));
    }

    public void addTag(String name, String attributes, Object value)
    {
        if (attributes == null || attributes.isEmpty())
        {
            addTag(name, value);
            return;
        }

        tabinate();

        if (value == null || (value instanceof String && ((String) value).isEmpty()))
        {
            buffer.append(String.format("<%s %s/>\n", name, attributes));
            return;
        }

        buffer.append(String.format("<%s %s>%s</%s>\n", name, attributes, value, name));
    }

    private void tabinate()
    {
        for (int i = 0; i < depth; ++i)
            buffer.append('\t');
    }

    @Override
    public String toString()
    {
        return buffer.toString();
    }

    public byte[] getBytes()
    {
        return buffer.toString().getBytes(StandardCharsets.UTF_8);
    }
}
