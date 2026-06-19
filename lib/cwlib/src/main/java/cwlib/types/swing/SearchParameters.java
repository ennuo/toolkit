package cwlib.types.swing;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.RowFilter.ComparisonType;

import cwlib.CwlibConfiguration;
import cwlib.enums.InventoryObjectType;
import cwlib.enums.ResourceType;
import cwlib.resources.RPlan;
import cwlib.resources.RTranslationTable;
import cwlib.resources.RCachedCostumeData.CachedCostumeData;
import cwlib.singleton.ResourceSystem;
import cwlib.types.SerializedResource;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDBRow;
import cwlib.util.Resources;
import cwlib.util.Strings;

/**
 * Toolkit search filtering settings
 */
public class SearchParameters
{
    private static final int MATCH_FILENAME = (1 << 0);
    private static final int DISABLE_WILDCARDS = (1 << 1);
    private static final int MATCH_REGEX = (1 << 2);
    private static final int CASE_SENSITIVE = (1 << 3);

    private static abstract class Condition
    {
        String pattern;

        protected Condition(String pattern)
        {
            this.pattern = pattern;
        }

        abstract boolean matches(FileNode node);
        
    };

    private static class SearchTermCondition extends Condition
    {

        int flags;
        boolean hasWildcards = false;
        Pattern regexPattern;

        boolean matchFilenames() { return (flags & MATCH_FILENAME) != 0; }
        boolean disableWildcards() { return (flags & DISABLE_WILDCARDS) != 0; }
        boolean caseSensitive() { return (flags & CASE_SENSITIVE) != 0; }
        boolean regex() { return (flags & MATCH_REGEX) != 0; }


        protected SearchTermCondition(String pattern, int flags) 
        { 
            super(pattern);
            this.flags = flags;

            if (regex())
            {
                regexPattern = Pattern.compile(pattern);
                return;
            }

            if (disableWildcards()) return;

            hasWildcards = pattern.contains("?") || pattern.contains("*");
        }

        @Override boolean matches(FileNode node)
        {
            if (pattern.length() == 0) return true;

            var entry = node.getEntry();
            var path = matchFilenames() ? entry.getName() : entry.getPath();
            
            if (regex())
                return regexPattern.matcher(path).find();
            
            if (!caseSensitive())
            {
                path = path.toLowerCase();
                pattern = pattern.toLowerCase();
            }
            
            if (!hasWildcards || disableWildcards()) 
                return path.contains(pattern);

            // lazily stolen

            int n = path.length();
            int m = pattern.length();

            int i = 0, j = 0, start = -1, match = 0;
            while (i < n)
            {
                if (j < m && (pattern.charAt(j) == '?' || pattern.charAt(j) == path.charAt(i)))
                {
                    i++;
                    j++;
                }
                else if (j < m && pattern.charAt(j) == '*')
                {
                    start = j;
                    match = i;
                    j++;
                }
                else if (start != -1)
                {
                    j = start + 1;
                    match++;
                    i = match;
                }
                else
                {
                    return false;
                }
            }

            while (j < m && pattern.charAt(j) == '*')
                ++j;

            return j == m;
        }
    }

    private static enum ComparisonType
    {
        GREATER_THAN,
        GREATER_THAN_EQUAL,
        LESS_THAN,
        LESS_THAN_EQUAL,
        EQUAL
    };

    private static class CachedInventoryData
    {
        public int type;
        public int subType;
        public int toolType;
        public int location;
        public int category;

        public CachedInventoryData(RPlan plan)
        {
            type = InventoryObjectType.getFlags(plan.inventoryData.type);
            subType = plan.inventoryData.subType;
            toolType = plan.inventoryData.toolType.getValue();
            location = (int)plan.inventoryData.location;
            category = (int)plan.inventoryData.category;
        }
    };

    private static HashMap<SHA1, Revision> RevisionCache = new HashMap<>();
    private static HashMap<SHA1, CachedInventoryData> InventoryCache = new HashMap<>();

    enum InventoryMatch
    {
        Category,
        Location,
        Type,
        SubType,
        Tool
    };

    private static class InventoryItemCondition extends Condition
    {
        private InventoryMatch _type;
        private int _key;


        public InventoryItemCondition(String query, InventoryMatch type)
        {
            super(query);
            _type = type;
            switch (type)
            {
                case Type:
                {
                    try
                    {
                        _key = Integer.parseInt(query);
                    }
                    catch (NumberFormatException ex)
                    {
                        _key = InventoryObjectType.valueOf(query.toUpperCase()).getValue();
                    }

                    break;
                }
                case Location:
                case Category:
                {
                    try
                    {
                        _key = Integer.parseInt(query);
                    }
                    catch (NumberFormatException ex)
                    {
                        _key = (int)RTranslationTable.makeLamsKeyID(query);
                    }

                    break;
                }
            }

        }


        @Override boolean matches(FileNode node) 
        {
            var entry = node.getEntry();

            if (!entry.getPath().endsWith(".plan")) return false;

            CachedInventoryData r = null;
            if (InventoryCache.containsKey(entry.getSHA1()))
            {
                r = InventoryCache.get(entry.getSHA1());
            }
            else
            {
                byte[] fileData = ResourceSystem.extract(node.getEntry());
                if (fileData != null)
                {
                    try
                    {
                        RPlan plan = new SerializedResource(fileData).loadResource(RPlan.class);
                        r = new CachedInventoryData(plan);
                    }
                    catch (Exception ex)
                    {

                    }
                }

                InventoryCache.put(entry.getSHA1(), r);
            }

            if (r == null) return false;

            switch (_type)
            {
                case Location: return r.location == _key;
                case Category: return r.category == _key;
                case Type: return (r.type & _key) != 0;
                case SubType: return (r.subType & _key) != 0;
                case Tool: return (r.toolType != 0);
            }

            return false;
        }
    }

    private static class RevisionCondition extends Condition
    {
        int revision;
        private ComparisonType type;
        private boolean branch;

        public RevisionCondition(String query, boolean branch)
        {
            super(query);
            this.branch = branch;
            if (query.startsWith(">="))
            {
                type = ComparisonType.GREATER_THAN_EQUAL;
                query = query.substring(2);
            }
            else if (query.startsWith("<="))
            {
                type = ComparisonType.LESS_THAN_EQUAL;
                query = query.substring(2);
            }
            else if (query.startsWith(">"))
            {
                type = ComparisonType.GREATER_THAN;
                query = query.substring(1);
            }
            else if (query.startsWith("<"))
            {
                type = ComparisonType.LESS_THAN;
                query = query.substring(1);
            }
            else type = ComparisonType.EQUAL;

            revision = (int)Strings.getLong(query);
        }

        @Override boolean matches(FileNode node)
        {
            var entry = node.getEntry();

            if (!entry.hasRevision()) return false;

            Revision r;
            if (RevisionCache.containsKey(entry.getSHA1()))
            {
                r = RevisionCache.get(entry.getSHA1());
            }
            else
            {
                byte[] fileData = ResourceSystem.extract(node.getEntry());
                r = Resources.getRevision(fileData);
                RevisionCache.put(entry.getSHA1(), r);
            }

            if (r == null) return false;

            int a = branch ? r.getBranchRevision() : r.getHead();
            int b = revision;

            switch (type)
            {
                case EQUAL: return a == b;
                case LESS_THAN: return a < b;
                case GREATER_THAN: return a > b;
                case GREATER_THAN_EQUAL: return a >= b;
                case LESS_THAN_EQUAL: return a <= b;
            }

            return false;
        }
    }

    private static class ResourceCondition extends Condition
    {

        private ResourceDescriptor descriptor;
        private boolean zeroes = false;
        private boolean checkExists = false;
        private ComparisonType type;

        public ResourceCondition(String query)
        {
            super(query);

            if (query.toLowerCase().equals("zero"))
            {
                descriptor = null;
                zeroes = true;
            }
            else if (query.toLowerCase().equals("exists"))
            {
                checkExists = true;
                descriptor = null;
            }
            else
            {
                if (query.startsWith(">="))
                {
                    type = ComparisonType.GREATER_THAN_EQUAL;
                    query = query.substring(2);
                }
                else if (query.startsWith("<="))
                {
                    type = ComparisonType.LESS_THAN_EQUAL;
                    query = query.substring(2);
                }
                else if (query.startsWith(">"))
                {
                    type = ComparisonType.GREATER_THAN;
                    query = query.substring(1);
                }
                else if (query.startsWith("<"))
                {
                    type = ComparisonType.LESS_THAN;
                    query = query.substring(1);
                }
                else type = ComparisonType.EQUAL;

                descriptor = new ResourceDescriptor(query, ResourceType.INVALID);
            }
        }

        @Override boolean matches(FileNode node) 
        {
            var entry = node.getEntry();
            if (entry == null) return false;

            if (zeroes)
            {
                return entry.getSHA1().equals(SHA1.EMPTY);
            }

            if (checkExists)
            {
                return ResourceSystem.exists(entry.getSHA1());
            }
            
            if (descriptor == null) return false;

            if (entry.getSource().getType().hasGUIDs() && descriptor.isGUID())
            {
                long a = ((GUID)entry.getKey()).getValue();
                long b = descriptor.getGUID().getValue();

                switch (type)
                {
                    case EQUAL: return a == b;
                    case LESS_THAN: return a < b;
                    case GREATER_THAN: return a > b;
                    case GREATER_THAN_EQUAL: return a >= b;
                    case LESS_THAN_EQUAL: return a <= b;
                }
            }
            else if (descriptor.isHash())
                return entry.getSHA1().equals(descriptor.getSHA1());

            return false;
        }        
    }

    private static class DateCondition extends Condition
    {
        enum DateComparisonType
        {
            DAY,
            MONTH,
            YEAR,
            DATE
        };


        private DateComparisonType dateType;
        private ComparisonType type;
        private DayOfWeek day;
        private Month month;
        private int year;
        private LocalDate date;

        private boolean test = true;

        public DateCondition(String query) 
        { 
            super(query);

            if (query.startsWith(">="))
            {
                type = ComparisonType.GREATER_THAN_EQUAL;
                query = query.substring(2);
            }
            else if (query.startsWith("<="))
            {
                type = ComparisonType.LESS_THAN_EQUAL;
                query = query.substring(2);
            }
            else if (query.startsWith(">"))
            {
                type = ComparisonType.GREATER_THAN;
                query = query.substring(1);
            }
            else if (query.startsWith("<"))
            {
                type = ComparisonType.LESS_THAN;
                query = query.substring(1);
            }
            else type = ComparisonType.EQUAL;

            switch (query.toLowerCase())
            {
                case "friday": { day = DayOfWeek.FRIDAY; break; }
                case "thursday": { day = DayOfWeek.THURSDAY; break; }
                case "wednesday": { day = DayOfWeek.WEDNESDAY; break; }
                case "tuesday": { day = DayOfWeek.TUESDAY; break; }
                case "monday": { day = DayOfWeek.MONDAY; break; }
                case "saturday": { day = DayOfWeek.SATURDAY; break; }
                case "sunday": { day = DayOfWeek.SUNDAY; break; }

                case "january": { month = java.time.Month.JANUARY; break; }
                case "february": { month = java.time.Month.FEBRUARY; break; }
                case "march": { month = java.time.Month.MARCH; break; }
                case "april": { month = java.time.Month.APRIL; break; }
                case "may": { month = java.time.Month.MAY; break; }
                case "june": { month = java.time.Month.JUNE; break; }
                case "july": { month = java.time.Month.JULY; break; }
                case "august": { month = java.time.Month.AUGUST; break; }
                case "september": { month = java.time.Month.SEPTEMBER; break; }
                case "october": { month = java.time.Month.OCTOBER; break; }
                case "november": { month = java.time.Month.NOVEMBER; break; }
                case "december": { month = java.time.Month.DECEMBER; break; }

                default: 
                {
                    if (query.length() == 4)
                    {
                        year = Integer.parseInt(query);
                    }
                    else
                    {
                        final DateTimeFormatterBuilder dtfb = new DateTimeFormatterBuilder();
                        dtfb.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0);

                        date = LocalDate.parse(query, dtfb.toFormatter());
                    }

                    break;
                }

            }

            if (date != null) dateType = DateComparisonType.DATE;
            else if (day != null) dateType = DateComparisonType.DAY;
            else if (month != null) dateType = DateComparisonType.MONTH;
            else if (year != 0) dateType = DateComparisonType.YEAR;

        }
        @Override boolean matches(FileNode node)
        {
            var entry = node.getEntry();
            if (!(entry instanceof FileDBRow row)) return false;

            var aDate = LocalDate.ofInstant(Instant.ofEpochMilli(row.getDate() * 1000L), ZoneId.systemDefault()).atStartOfDay();
            int a = 0, b = 0;
            switch (dateType)
            {
                case DAY:
                {
                    a = aDate.getDayOfWeek().getValue();
                    b = day.getValue();
                    break;
                }
                case MONTH:
                {
                    a = aDate.getDayOfMonth();
                    b = month.getValue();
                    break;
                }
                case YEAR:
                {
                    a = aDate.getYear();
                    b = year;
                    break;
                }
                case DATE:
                {

                    var bDate = date.atStartOfDay();
                    switch (type)
                    {
                        case EQUAL: return aDate.isEqual(bDate);
                        case LESS_THAN: return aDate.compareTo(bDate) < 0;
                        case GREATER_THAN: return aDate.compareTo(bDate) > 0;
                        case GREATER_THAN_EQUAL: return aDate.compareTo(bDate) >= 0;
                        case LESS_THAN_EQUAL: return aDate.compareTo(bDate) <= 0;
                    }
                    
                }
            }

            switch (type)
            {
                case EQUAL: return a == b;
                case LESS_THAN: return a < b;
                case GREATER_THAN: return a > b;
                case GREATER_THAN_EQUAL: return a >= b;
                case LESS_THAN_EQUAL: return a <= b;
            }


            return false;
        }
    }

    private static class StartsWithCondition extends Condition
    {
        public StartsWithCondition(String query) { super(query); }
        @Override boolean matches(FileNode node)
        {
            return node.getEntry().getName().toLowerCase().startsWith(pattern);
        }
    }

    private static class TextureCondition extends Condition
    {
        public TextureCondition(String query) { super(query); }

        @Override boolean matches(FileNode node) 
        {
            var path = node.getEntry().getPath();
            return path.endsWith(".tex") || path.endsWith(".dds") || path.endsWith(".jpg") || path.endsWith(".png");
        }        
    }

    private static class AudioCondition extends Condition
    {
        public AudioCondition(String query) { super(query); }

        @Override boolean matches(FileNode node) 
        {
            var path = node.getEntry().getPath();
            return path.endsWith(".wav") || path.endsWith(".fsb") || path.endsWith(".mp3") || path.endsWith(".smp");
        }        
    }

    private static class Or extends Condition
    {
        Condition a;
        Condition b;

        public Or(Condition a, Condition b)
        {
            super(null);
            this.a = a;
            this.b = b;

            if (a == null || b == null)
                throw new RuntimeException("Passing NULL to binary operator!");
        }

        @Override public boolean matches(FileNode node)
        {
            return a.matches(node) || b.matches(node);
        }
    }

    private static class And extends Condition
    {
        Condition a;
        Condition b;

        public And(Condition a, Condition b)
        {
            super(null);
            this.a = a;
            this.b = b;

            if (a == null || b == null)
                throw new RuntimeException("Passing NULL to binary operator!");
        }

        @Override public boolean matches(FileNode node)
        {
            return a.matches(node) && b.matches(node);
        }
    }

    private static class Invert extends Condition
    {
        Condition a;

        public Invert(Condition a)
        {
            super(null);
            if (a == null)
                throw new RuntimeException("Can't invert null condition!");
            this.a = a;
        }

        @Override public boolean matches(FileNode node)
        {
            return !a.matches(node);
        }
    }

    private Condition condition = null;
    private String query;
    private int i;
    private StringBuilder builder = new StringBuilder(256);

    private String readToken()
    {
        builder.setLength(0);
        if (i >= query.length()) return null;

        boolean inQuotes = false;
        if (query.charAt(i) == '"')
        {
            inQuotes = true;
            ++i;
        }

        while (i < query.length())
        {
            char c = query.charAt(i);
            if (inQuotes && c == '"')
            {
                i++;
                break;
            }
            else if (!inQuotes && c == ' ' || c == '|')
            {
                break;
            }

            builder.append(c);
            ++i;

            if (!inQuotes && c == ':') break;
        }

        return builder.toString();
    }

    private Condition parse()
    {
        if (query.charAt(i) == '!') 
        {
            i += 1;
            return new Invert(parse());
        }

        int flags = 0;
        
        String token = null;
        boolean mod = true;
        while (mod)
        {
            token = readToken();
            switch (token.toLowerCase())
            {
                case "nowildcards:": flags |= DISABLE_WILDCARDS; break;
                case "nopath:": flags |= MATCH_FILENAME; break;
                case "regex:": flags |= MATCH_REGEX; break;
                case "case:": flags |= CASE_SENSITIVE; break;
                default: 
                    mod = false; 
                    break;
            }
        }

        Condition c = null;
        switch (token.toLowerCase())
        {
            case "res:": c = new ResourceCondition(readToken()); break;
            case "texture:": c = new TextureCondition(null); break;
            case "audio:": c = new AudioCondition(null); break;
            case "startwith:": 
            case "startswith:":
                c = new StartsWithCondition(readToken()); break;
            case "revision:": c = new RevisionCondition(readToken(), false); break;
            case "branch:": c = new RevisionCondition(readToken(), true); break;
            case "date:": c = new DateCondition(readToken()); break;
            case "tool:": c = new InventoryItemCondition(null, InventoryMatch.Tool); break;
            case "type:": c = new InventoryItemCondition(readToken(), InventoryMatch.Type); break;
            case "subtype:": c = new InventoryItemCondition(readToken(), InventoryMatch.SubType); break;
            case "location:": c = new InventoryItemCondition(readToken(), InventoryMatch.Location); break;
            case "category:": c = new InventoryItemCondition(readToken(), InventoryMatch.Category); break;
            default: c = new SearchTermCondition(token, flags); break;
        }

        return c;
    }

    /**
     * Constructs search parameters from a query
     *
     * @param input Search query
     */
    public SearchParameters(String input)
    {
        query = input.trim();
        if (query.startsWith("/"))
        {
            switch (query.substring(1).toLowerCase())
            {
                case "clear_cache":
                {
                    RevisionCache = new HashMap<>();
                    InventoryCache = new HashMap<>();
                    JOptionPane.showMessageDialog(null, "Cleared revision cache!");
                    return;
                }
                case "idiot":
                {
                    JOptionPane.showMessageDialog(null, "just wiped your game install man");
                    return;
                }
            }
        }

        try
        {
            if (!query.isEmpty())
            {
                condition = parse();
                while (i < query.length())
                {
                    switch (query.charAt(i++))
                    {
                        case '|': condition = new Or(condition, parse()); break;
                        case ' ': condition = new And(condition, parse()); break;
                        default: throw new RuntimeException("invalid operator");
                    }
                }
            }
        }
        catch (Exception ex)
        {
            condition = null;
        }
    }

    public boolean matches(FileNode node)
    {
        if (condition == null) return true;
        return condition.matches(node);
    }

    public boolean empty()
    {
        return condition == null;
    }
}
