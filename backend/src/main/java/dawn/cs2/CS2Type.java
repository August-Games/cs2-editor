package dawn.cs2;

import dawn.cs2.ast.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class CS2Type {
    
    public static CS2Type ANIM = new CS2Type(1, 0, 0, "Animation", 'A');
    public static CS2Type AREA = new CS2Type(1, 0, 0, "Area", 'R');
    public static CS2Type BOOLEAN = new CS2Type(1, 0, 0, "boolean", '1');
    public static CS2Type CALLBACK = new CS2Type(0, 0, 0, "Callback", '\0'); //not real type
    public static CS2Type CATEGORY = new CS2Type(1, 0, 0, "Category", 'y');
    public static CS2Type CHAR = new CS2Type(1, 0, 0, "char", 'z');
    public static CS2Type COLOR = new CS2Type(1, 0, 0, "Color", 'i'); //Not a real type, but helps us know where we need to convert int to hex notation
    public static CS2Type CONTAINER = new CS2Type(1, 0, 0, "Container", 'v');
    public static CS2Type DB_COLUMN = new CS2Type(1, 0, 0, "DbColumn", 'i');
    public static CS2Type DB_FIELD = new CS2Type(1, 0, 0, "DbField", 'i');
    public static CS2Type DB_ROW = new CS2Type(1, 0, 0, "DbRow", 'Ð');
    public static CS2Type DB_TABLE = new CS2Type(1, 0, 0, "DbTable", 'i');
    public static CS2Type ENUM = new CS2Type(1, 0, 0, "Enum", 'g');
    public static CS2Type FONTMETRICS = new CS2Type(1, 0, 0, "FontMetrics", 'f');
    public static CS2Type GRAPHIC = new CS2Type(1, 0, 0, "Graphic", 't');
    public static CS2Type INT = new CS2Type(1, 0, 0, "int", 'i');
    public static CS2Type INT_ARRAY = new CS2Type(1, 0, 0, "int[]", '\0');//not real type
    public static CS2Type ITEM = new CS2Type(1, 0, 0, "Item", 'o');
    public static CS2Type ITEM_ID = new CS2Type(1, 0, 0, "ItemId", 'i');
    public static CS2Type LOCATION = new CS2Type(1, 0, 0, "Location", 'c');
    public static CS2Type LOCSHAPE = new CS2Type(1, 0, 0, "LocShape", 'H');
    public static CS2Type LONG = new CS2Type(0, 0, 1, "long", '§'); // TODO remove, not supported by osrs
    public static CS2Type LONG_ARRAY = new CS2Type(1, 0, 0, "long[]", '\0');//not real type
    public static CS2Type MAPID = new CS2Type(1, 0, 0, "Map", '`');
    public static CS2Type MAP_ELEMENT = new CS2Type(1, 0, 0, "MapElement", 'µ');
    public static CS2Type MODEL = new CS2Type(1, 0, 0, "Model", 'm');
    public static CS2Type NAMED_ITEM = new CS2Type(1, 0, 0, "NamedItem", 'O');
    public static CS2Type NPCDEF = new CS2Type(1, 0, 0, "NpcDef", 'n');
    public static CS2Type NPCUID = new CS2Type(1, 0, 0, "NpcUid", 'u');
    public static CS2Type OBJECT = new CS2Type(1, 0, 0, "Object", 'l');
    public static CS2Type OVERLAY_INTERFACE = new CS2Type(1, 0, 0, "OverlayInterface", 'L');
    public static CS2Type SKILL = new CS2Type(1, 0, 0, "Skill", 'S');
    public static CS2Type SOUNDEFFECT = new CS2Type(1, 0, 0, "SoundEff", 'P');
    public static CS2Type SPRITE = new CS2Type(1, 0, 0, "Sprite", 'd');
    public static CS2Type STRING = new CS2Type(0, 1, 0, "string", 's');
    public static CS2Type STRING_ARRAY = new CS2Type(1, 0, 0, "string[]", '\0');//not real type
    public static CS2Type STRUCT = new CS2Type(1, 0, 0, "Struct", 'J');
    public static CS2Type TEXTURE = new CS2Type(1, 0, 0, "Texture", 'x');
    public static CS2Type TOPLEVEL_INTERFACE = new CS2Type(1, 0, 0, "TopLevelInterface", 'F');
    public static CS2Type UNKNOWN = new CS2Type(0, 0, 0, "??", '\0');
    public static CS2Type VOID = new CS2Type(0, 0, 0, "void", '\0');
    public static CS2Type WIDGET_PTR = new CS2Type(1, 0, 0, "Widget", 'I');
    public static CS2Type[] TYPE_LIST = new CS2Type[]{VOID, CALLBACK, BOOLEAN, INT, FONTMETRICS, SPRITE, MODEL, LOCATION, CHAR, STRING, LONG, UNKNOWN, WIDGET_PTR, ITEM_ID, ITEM, NAMED_ITEM, COLOR, CONTAINER, ENUM, STRUCT, ANIM, MAPID, GRAPHIC, SKILL, NPCDEF, TEXTURE, CATEGORY, SOUNDEFFECT, INT_ARRAY, LONG_ARRAY, STRING_ARRAY, DB_ROW, DB_FIELD, DB_COLUMN, DB_TABLE, OBJECT, MAP_ELEMENT, AREA, LOCSHAPE, NPCUID, OVERLAY_INTERFACE, TOPLEVEL_INTERFACE};
    static Map<Integer, CS2Type> attrTypes = new HashMap<>();
    private static final List<CS2Type> cache = new ArrayList<CS2Type>();

    static {
        try {
            InputStreamReader ir = new InputStreamReader(CS2Type.class.getResourceAsStream("/cs2/attr.types.txt"));
            BufferedReader br = new BufferedReader(ir);
            String l;
            while ((l = br.readLine()) != null) {
                String[] t = l.split(" ");
                CS2Type.attrTypes.put(Integer.parseInt(t[0]), CS2Type.forJagexDesc(t[1].charAt(0)));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public char charDesc;
    public List<CS2Type> composite = new ArrayList<>();
    private int intStackSize;
    private int stringStackSize;
    private int longStackSize;
    private final String name;
    private final boolean structure;
    
    //TODO: Refactor this
    public CS2Type(int iss, int sss, int lss, String name, char c) {
        this.intStackSize = iss;
        this.stringStackSize = sss;
        this.longStackSize = lss;
        this.name = name;
        this.charDesc = c;
        this.structure = false;
        composite.add(this);
    }
    
    private CS2Type(List<CS2Type> struct) {
        for (CS2Type t : struct) {
            this.intStackSize += t.intStackSize;
            this.stringStackSize += t.stringStackSize;
            this.longStackSize += t.longStackSize;
            composite.addAll(t.composite);
        }
        structure = true;
        name = "";
        cache.add(this);
    }
    
    public static CS2Type of(List<CS2Type> typeList) {
        if (typeList.size() == 1) {
            return typeList.get(0);
        }
        find:
        for (CS2Type other : cache) {
            if (other.composite.size() != typeList.size()) {
                continue;
            }
            for (int i = 0; i < other.composite.size(); i++) {
                if (other.composite.get(i) != typeList.get(i)) {
                    continue find;
                }
            }
            return other;
        }
        return new CS2Type(typeList);
    }
    
    public static CS2Type typeFor(List<ExpressionNode> list) {
        List<CS2Type> result = new ArrayList<>();
        for (ExpressionNode n : list) {
            result.addAll(n.getType().composite);
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        
        return CS2Type.of(result);
    }
    
    /**
     * Casts expression node to specific type.
     * If expression type is same then returned value is expr,
     * otherwise on most cases CastExpressionNode is returned with one child
     * which is expr.
     */
    public static ExpressionNode cast(ExpressionNode expr, CS2Type type) {
        if (expr.getType().equals(type))
            return expr;
        if (expr instanceof PlaceholderValueNode) {
            throw new DecompilerException("Can't cast placeholder values!");
        }
        
        if (type.equals(UNKNOWN)) {
            return expr;
        }
        
        if (type.equals(LOCATION))
            return new NewLocationNode(expr);
        if (type.equals(WIDGET_PTR))
            return new NewWidgetPointerNode(expr);
        if (type.equals(COLOR))
            return new NewColorNode(expr instanceof NewWidgetPointerNode ? ((NewWidgetPointerNode) expr).getExpression() : expr);
        if (type.equals(BOOLEAN) && expr instanceof IntExpressionNode) {
            int val = ((IntExpressionNode) expr).getData();
            /*if (val > 1 || val < -1) {
                throw new DecompilerException("Cannot cast to boolean " + val);
            }*/
            if (val == -1) {
                System.err.println("warning null boolean");
                return new NullableIntExpressionNode(-1);
//                throw new DecompilerException("-1 boolean?");
            }
            return new BooleanExpressionNode(val != 0); //not 1 is truthy //TODO: -1 is NULL? not 0 or 1 is faulty?
        }
        if (type.equals(CHAR) && expr instanceof IntExpressionNode)
            return new CharExpressionNode((char) ((IntExpressionNode) expr).getData());
        
        if ((expr.getType() == BOOLEAN || expr.getType() == COLOR) && type == INT) {
            //allow implicit conversion from these types to int
            return expr;
        }
        //Implicit coercion for this, but -1 represents nulls
        if (expr instanceof IntExpressionNode && type.isCompatible(CS2Type.INT)) {//(type.equals(SPRITE) || type.equals(FONTMETRICS) || type.equals(ITEM) || type.equals(MODEL) || type.equals(CONTAINER) || type.equals(ANIM) || type.equals(MAPID) || type.equals(GRAPHIC) || type.equals(SKILL) || type.equals(NPCDEF) || type.equals(QCPHRASE) || type.equals(CHATCAT) || type.equals(TEXTURE) || type.equals(STANCE) || type.equals(CATEGORY) || type.equals(SOUNDEFFECT))) {
            return new NullableIntExpressionNode(((IntExpressionNode) expr).getData());
        }
        if (type.isCompatible(expr.getType())) {
            return new CastNode(type, expr);
        }
        
        throw new DecompilerException("Incompatible cast " + expr.getType() + " to " + type);
    }
    
    public static CS2Type forDesc(String desc) {
        for (int i = 0; i < TYPE_LIST.length; i++) {
            if (desc.equals(TYPE_LIST[i].toString())) {
                return TYPE_LIST[i];
            }
        }
        if (!desc.contains("{"))
            return null;
        String[] spl = desc.split("\\{");
        String name = spl[0];
        String stackDesc = spl[1].substring(0, spl[1].length() - 1);
        String[] stackSpl = stackDesc.split("\\,");
        
        List<CS2Type> composite = new LinkedList<>();
        for (String s : stackSpl) {
            composite.add(forDesc(s.trim()));
        }
        
        return CS2Type.of(composite);
    }
    
    public static CS2Type forJagexDesc(char desc) {
        switch (desc) {
            case '\0':
                return CS2Type.UNKNOWN;
//                return VOID;
            case '§':
                return LONG;
            case 'i':
                return INT;
            case 'z':
                return CHAR;
            case 's':
                return STRING;
            case '1':
                return BOOLEAN;
            case 'o':
                return ITEM;
            case 'O':
                return NAMED_ITEM;
            case 'A':
                return ANIM;
            case 'S':
                return SKILL;
            case 't':
                return GRAPHIC;
            case 'c':
                return LOCATION;
            case 'n':
                return NPCDEF;
            case 'J':
                return STRUCT;
            case 'g':
                return ENUM;
            case 'f':
                return FONTMETRICS;
            case 'd':
                return SPRITE;
            case 'm':
                return MODEL;
            case 'v':
                return CONTAINER;
            case 'I':
                return WIDGET_PTR;
            case 'x':
                return TEXTURE;
            case '`':
                return MAPID;
            case 'y':
                return CATEGORY;
            case 'P':
                return SOUNDEFFECT;
            case 'Ð':
                return DB_ROW;
            case 'l':
                return OBJECT;
            case 'µ':
                return MAP_ELEMENT;
            case 'R':
                return AREA;
            case 'H':
                return LOCSHAPE;
            case 'u':
                return NPCUID;
            case 'L':
                return OVERLAY_INTERFACE;
            case 'F':
                return TOPLEVEL_INTERFACE;
            default:
                return INT;
        }
        
    }
    
    public boolean isStructure() {
        return structure;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public int hashCode() {
        int stackHash = structure ? (1 << 9) : 0;
        stackHash |= intStackSize & 0x7;
        stackHash |= (stringStackSize & 0x7) << 3;
        stackHash |= (longStackSize & 0x7) << 6;
        int nameHash = (name.length() & 0x7) | (name.length() << 3);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c & 0x1) != 0)
                nameHash += nameHash + (nameHash / c);
            else
                nameHash += nameHash - (nameHash * c);
        }
        return stackHash | (nameHash << 11);
    }
    
    public boolean equals(CS2Type other) {
        if (this.structure != other.structure) {
            return false;
        }
        if (this.composite.size() != other.composite.size()) {
            return false;
        }
        for (int i = 0; i < composite.size(); i++) {
            if (composite.get(i) != other.composite.get(i)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isCompatible(CS2Type other) {
        //TODO: Order should be same too, but only used for simple types?
        return this == UNKNOWN || other == CS2Type.UNKNOWN || (intStackSize == other.intStackSize && stringStackSize == other.stringStackSize && longStackSize == other.longStackSize);
    }
    
    @Override
    public String toString() {
        if (structure) {
            StringBuilder s = new StringBuilder();
            boolean first = true;
            for (CS2Type t : composite) {
                if (!first) {
                    s.append(", ");
                }
                first = false;
                s.append(t);
            }
            return s.toString();
        } else {
            return this.name;
        }
    }
    
}
