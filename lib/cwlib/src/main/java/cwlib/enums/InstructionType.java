package cwlib.enums;

import java.util.ArrayList;
import java.util.Collections;

public enum InstructionType
{
    NOP(0x0),
    LCb(0x1),
    LCc(0x2),
    LCi(0x3),
    LCf(0x4),
    LCsw(0x5),
    LC_NULLsp(0x6),
    MOVb(0x7),
    LOG_NEGb(0x8),
    MOVc(0x9),
    MOVi(0xa),
    INCi(0xb),
    DECi(0xc),
    NEGi(0xd),
    BIT_NEGi(0xe),
    LOG_NEGi(0xf),
    ABSi(0x10),
    MOVf(0x11),
    NEGf(0x12),
    ABSf(0x13),
    SQRTf(0x14),
    SINf(0x15),
    COSf(0x16),
    TANf(0x17),
    MOVv4(0x18),
    NEGv4(0x19),
    MOVm44(0x1a),
    @Deprecated MOVs(0x1b),
    MOVrp(0x1c),
    MOVcp(0x1d),
    MOVsp(0x1e),
    MOVo(0x1f),
    EQb(0x20),
    NEb(0x21),
    RESERVED0(0x22),
    RESERVED1(0x23),
    LTc(0x24),
    LTEc(0x25),
    GTc(0x26),
    GTEc(0x27),
    EQc(0x28),
    NEc(0x29),
    ADDi(0x2a),
    SUBi(0x2b),
    MULi(0x2c),
    DIVi(0x2d),
    MODi(0x2e),
    MINi(0x2f),
    MAXi(0x30),
    SLAi(0x31),
    SRAi(0x32),
    SRLi(0x33),
    BIT_ORi(0x34),
    BIT_ANDi(0x35),
    BIT_XORi(0x36),
    LTi(0x37),
    LTEi(0x38),
    GTi(0x39),
    GTEi(0x3a),
    EQi(0x3b),
    NEi(0x3c),
    ADDf(0x3d),
    SUBf(0x3e),
    MULf(0x3f),
    DIVf(0x40),
    MINf(0x41),
    MAXf(0x42),
    LTf(0x43),
    LTEf(0x44),
    GTf(0x45),
    GTEf(0x46),
    EQf(0x47),
    NEf(0x48),
    ADDv4(0x49),
    SUBv4(0x4a),
    MULSv4(0x4b),
    DIVSv4(0x4c),
    DOT4v4(0x4d),
    DOT3v4(0x4e),
    DOT2v4(0x4f),
    CROSS3v4(0x50),
    MULm44(0x51),
    @Deprecated EQs(0x52),
    @Deprecated NEs(0x53),
    EQrp(0x54),
    NErp(0x55),
    EQo(0x56),
    NEo(0x57),
    EQsp(0x58),
    NEsp(0x59),
    GET_V4_X(0x5a),
    GET_V4_Y(0x5b),
    GET_V4_Z(0x5c),
    GET_V4_W(0x5d),
    GET_V4_LEN2(0x5e),
    GET_V4_LEN3(0x5f),
    GET_V4_LEN4(0x60),
    GET_M44_XX(0x61),
    GET_M44_XY(0x62),
    GET_M44_XZ(0x63),
    GET_M44_XW(0x64),
    GET_M44_YX(0x65),
    GET_M44_YY(0x66),
    GET_M44_YZ(0x67),
    GET_M44_YW(0x68),
    GET_M44_ZX(0x69),
    GET_M44_ZY(0x6a),
    GET_M44_ZZ(0x6b),
    GET_M44_ZW(0x6c),
    GET_M44_WX(0x6d),
    GET_M44_WY(0x6e),
    GET_M44_WZ(0x6f),
    GET_M44_WW(0x70),
    GET_M44_rX(0x71),
    GET_M44_rY(0x72),
    GET_M44_rZ(0x73),
    GET_M44_rW(0x74),
    GET_M44_cX(0x75),
    GET_M44_cY(0x76),
    GET_M44_cZ(0x77),
    GET_M44_cW(0x78),
    SET_V4_X(0x79),
    SET_V4_Y(0x7a),
    SET_V4_Z(0x7b),
    SET_V4_W(0x7c),
    SET_M44_XX(0x7d),
    SET_M44_XY(0x7e),
    SET_M44_XZ(0x7f),
    SET_M44_XW(0x80),
    SET_M44_YX(0x81),
    SET_M44_YY(0x82),
    SET_M44_YZ(0x83),
    SET_M44_YW(0x84),
    SET_M44_ZX(0x85),
    SET_M44_ZY(0x86),
    SET_M44_ZZ(0x87),
    SET_M44_ZW(0x88),
    SET_M44_WX(0x89),
    SET_M44_WY(0x8a),
    SET_M44_WZ(0x8b),
    SET_M44_WW(0x8c),
    SET_M44_rX(0x8d),
    SET_M44_rY(0x8e),
    SET_M44_rZ(0x8f),
    SET_M44_rW(0x90),
    SET_M44_cX(0x91),
    SET_M44_cY(0x92),
    SET_M44_cZ(0x93),
    SET_M44_cW(0x94),
    GET_SP_MEMBER(0x95),
    GET_RP_MEMBER(0x96),
    SET_SP_MEMBER(0x97),
    SET_RP_MEMBER(0x98),
    GET_ELEMENT(0x99),
    SET_ELEMENT(0x9a),
    GET_ARRAY_LEN(0x9b),
    NEW_ARRAY(0x9c),
    ARRAY_INSERT(0x9d),
    ARRAY_APPEND(0x9e),
    ARRAY_ERASE(0x9f),
    ARRAY_FIND(0xa0),
    ARRAY_CLEAR(0xa1),
    WRITE(0xa2),
    ARG(0xa3),
    CALL(0xa4),
    RETURN(0xa5),
    B(0xa6),
    BEZ(0xa7),
    BNEZ(0xa8),
    CASTsp(0xa9),
    INTb(0xaa),
    INTc(0xab),
    INTf(0xac),
    FLOATb(0xad),
    FLOATc(0xae),
    FLOATi(0xaf),
    BOOLc(0xb0),
    BOOLi(0xb1),
    BOOLf(0xb2),
    GET_OBJ_MEMBER(0xb3),
    SET_OBJ_MEMBER(0xb4),
    NEW_OBJECT(0xb5),
    ARRAY_RESIZE(0xb6),
    ARRAY_RESERVE(0xb7),
    LCv4(0xb8),
    LC_NULLo(0xb9),
    CASTo(0xba),
    GET_SP_NATIVE_MEMBER(0xbb),
    LCsa(0xbc),
    BIT_ORb(0xbd),
    BIT_ANDb(0xbe),
    BIT_XORb(0xbf),
    CALLVo(0xc0),
    CALLVsp(0xc1),
    ASSERT(0xc2),
    LCs64(0xc3),
    MOVs64(0xc4),
    ADDs64(0xc5),
    EQs64(0xc6),
    NEs64(0xc7),
    BIT_ORs64(0xc8),
    BIT_ANDs64(0xc9),
    BIT_XORs64(0xca);

    private static final InstructionClass[] INSTRUCTION_CLASSES;

    static
    {
        ArrayList<InstructionClass> classList = new ArrayList<>();

        classList.add(InstructionClass.NOP);
        classList.addAll(Collections.nCopies(6, InstructionClass.LOAD_CONST));
        classList.addAll(Collections.nCopies(20, InstructionClass.UNARY));
        classList.add(InstructionClass.NOP);
        classList.addAll(Collections.nCopies(4, InstructionClass.UNARY));
        classList.addAll(Collections.nCopies(50, InstructionClass.BINARY));
        classList.add(InstructionClass.NOP);
        classList.add(InstructionClass.NOP);
        classList.addAll(Collections.nCopies(6, InstructionClass.BINARY));
        classList.addAll(Collections.nCopies(31, InstructionClass.GET_BUILTIN_MEMBER));
        classList.addAll(Collections.nCopies(28, InstructionClass.SET_BUILTIN_MEMBER));
        classList.addAll(Collections.nCopies(2, InstructionClass.GET_MEMBER));
        classList.addAll(Collections.nCopies(2, InstructionClass.SET_MEMBER));
        classList.add(InstructionClass.GET_ELEMENT);
        classList.add(InstructionClass.SET_ELEMENT);
        classList.add(InstructionClass.GET_BUILTIN_MEMBER);
        classList.add(InstructionClass.NEW_ARRAY);
        classList.addAll(Collections.nCopies(3, InstructionClass.SET_ELEMENT));
        classList.add(InstructionClass.GET_ELEMENT);
        classList.add(InstructionClass.SET_ELEMENT);
        classList.add(InstructionClass.WRITE);
        classList.add(InstructionClass.ARG);
        classList.add(InstructionClass.CALL);
        classList.add(InstructionClass.RETURN);
        classList.addAll(Collections.nCopies(3, InstructionClass.BRANCH));
        classList.add(InstructionClass.CAST);
        classList.addAll(Collections.nCopies(9, InstructionClass.UNARY));
        classList.add(InstructionClass.GET_MEMBER);
        classList.add(InstructionClass.SET_MEMBER);
        classList.add(InstructionClass.NEW_OBJECT);
        classList.addAll(Collections.nCopies(2, InstructionClass.SET_ELEMENT));
        classList.addAll(Collections.nCopies(2, InstructionClass.LOAD_CONST));
        classList.add(InstructionClass.CAST);
        classList.add(InstructionClass.GET_MEMBER);
        classList.add(InstructionClass.LOAD_CONST);
        classList.addAll(Collections.nCopies(3, InstructionClass.BINARY));
        classList.addAll(Collections.nCopies(2, InstructionClass.CALL));
        classList.add(InstructionClass.WRITE);
        classList.add(InstructionClass.LOAD_CONST);
        classList.add(InstructionClass.UNARY);
        classList.addAll(Collections.nCopies(6, InstructionClass.BINARY));

        INSTRUCTION_CLASSES = classList.toArray(InstructionClass[]::new);
    }

    private final int value;

    InstructionType(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return this.value;
    }

    public static InstructionType fromValue(int value)
    {
        if (value < 0) return null;
        InstructionType[] values = InstructionType.values();
        if (value >= values.length) return null;
        return values[value];
    }

    public InstructionClass getInstructionClass()
    {
        return INSTRUCTION_CLASSES[this.value];
    }

    public static void main(String[] args)
    {
        for (InstructionType type : InstructionType.values())
        {
            if (type.getInstructionClass().equals(InstructionClass.NEW_ARRAY))
            {
                System.out.println(type);
            }
        }
    }
}
