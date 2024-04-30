package cwlib.resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

import cwlib.enums.InstructionType;
import cwlib.enums.ModifierType;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.script.*;
import cwlib.structs.script.instructions.*;

public class RScript implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public String className;
    public ResourceDescriptor superClassScript;
    public EnumSet<ModifierType> modifiers = EnumSet.noneOf(ModifierType.class);
    public ArrayList<TypeReferenceRow> typeReferences = new ArrayList<>();
    public ArrayList<FieldReferenceRow> fieldReferences = new ArrayList<>();
    public ArrayList<FunctionReferenceRow> functionReferences = new ArrayList<>();
    public ArrayList<FieldDefinitionRow> fieldDefinitions = new ArrayList<>();
    public ArrayList<PropertyDefinitionRow> propertyDefinitions = new ArrayList<>();
    public ArrayList<FunctionDefinitionRow> functionDefinitions = new ArrayList<>();
    public ArrayList<TypeOffset> sharedArguments = new ArrayList<>();
    public ArrayList<Instruction> sharedBytecode = new ArrayList<>();
    public ArrayList<Short> sharedLineNos = new ArrayList<>();
    public ArrayList<LocalVariableDefinitionRow> sharedLocalVariables = new ArrayList<>();
    public ArrayList<String> stringATable = new ArrayList<>();
    public ArrayList<String> stringWTable = new ArrayList<>();
    public ArrayList<Long> constantTableS64 = new ArrayList<>();
    public ArrayList<Float> constantTable = new ArrayList<>();
    public ArrayList<GUID> dependingGUIDs = new ArrayList<>();

    @Override
    public void serialize(Serializer serializer)
    {

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();

        if (version <= 0x1eb)
            throw new SerializationException("Inline script format not supported!");

        if (version <= 0x33a)
            serializer.resource(null, ResourceType.SCRIPT); // upToDateScript

        className = serializer.str(className);
        superClassScript = serializer.resource(superClassScript, ResourceType.SCRIPT);

        if (serializer.isWriting())
        {
            short flags = ModifierType.getFlags(modifiers);
            if (version >= 0x3d9) serializer.getOutput().i16(flags);
            else serializer.getOutput().i32(flags);
        }
        else
        {
            int flags = (version >= 0x3d9) ? serializer.getInput().i16() :
                serializer.getInput().i32();
            modifiers = ModifierType.fromValue(flags);
        }

        typeReferences = serializer.arraylist(typeReferences, TypeReferenceRow.class);
        fieldReferences = serializer.arraylist(fieldReferences,
            FieldReferenceRow.class);
        functionReferences = serializer.arraylist(functionReferences,
            FunctionReferenceRow.class);
        fieldDefinitions = serializer.arraylist(fieldDefinitions,
            FieldDefinitionRow.class);
        propertyDefinitions = serializer.arraylist(propertyDefinitions,
            PropertyDefinitionRow.class);
        functionDefinitions = serializer.arraylist(functionDefinitions,
            FunctionDefinitionRow.class);
        sharedArguments = serializer.arraylist(sharedArguments, TypeOffset.class);

        if (serializer.isWriting())
        {
            MemoryOutputStream stream = serializer.getOutput();
            stream.i32(sharedBytecode.size());
            for (Instruction instruction : sharedBytecode)
                stream.u64(instruction.getBits());
        }
        else
        {
            MemoryInputStream stream = serializer.getInput();
            int count = stream.i32();
            sharedBytecode = new ArrayList<>(count);
            for (int i = 0; i < count; ++i)
            {
                long code = stream.u64();
                InstructionType type = InstructionType.fromValue((int) (code & 0xff));
                Instruction instruction = null;
                switch (type.getInstructionClass())
                {
                    case NOP:
                        instruction = new NopInstruction(code);
                        break;
                    case LOAD_CONST:
                    {
                        switch (type)
                        {
                            case LCb:
                                instruction = new LoadConstInstructionBool(code);
                                break;
                            case LCc:
                                instruction = new LoadConstInstructionChar(code);
                                break;
                            case LCf:
                                instruction = new LoadConstInstructionFloat(code);
                                break;
                            case LCi:
                                instruction = new LoadConstInstructionInt(code);
                                break;
                            case LC_NULLsp:
                            case LC_NULLo:
                                instruction =
                                    new LoadConstInstructionNullSafePtr(code);
                                break;
                            default:
                                instruction = new LoadConstInstruction(code);
                                break;
                        }
                        break;
                    }
                    case CAST:
                        instruction = new CastInstruction(code);
                        break;
                    case UNARY:
                        instruction = new UnaryInstruction(code);
                        break;
                    case BINARY:
                        instruction = new BinaryInstruction(code);
                        break;
                    case GET_BUILTIN_MEMBER:
                        instruction = new GetBuiltInMemberInstruction(code);
                        break;
                    case SET_BUILTIN_MEMBER:
                        instruction = new SetBuiltInMemberInstruction(code);
                        break;
                    case GET_MEMBER:
                        instruction = new GetMemberInstruction(code);
                        break;
                    case SET_MEMBER:
                        instruction = new SetMemberInstruction(code);
                        break;
                    case GET_ELEMENT:
                        instruction = new GetElementInstruction(code);
                        break;
                    case SET_ELEMENT:
                        instruction = new SetElementInstruction(code);
                        break;
                    case NEW_OBJECT:
                        instruction = new NewObjectInstruction(code);
                        break;
                    case NEW_ARRAY:
                        instruction = new NewArrayInstruction(code);
                        break;
                    case WRITE:
                        instruction = new WriteInstruction(code);
                        break;
                    case ARG:
                        instruction = new ArgInstruction(code);
                        break;
                    case CALL:
                        instruction = new CallInstruction(code);
                        break;
                    case RETURN:
                        instruction = new ReturnInstruction(code);
                        break;
                    case BRANCH:
                        instruction = new BranchInstruction(code);
                        break;
                }
                sharedBytecode.add(instruction);
            }
        }

        if (serializer.isWriting())
        {
            MemoryOutputStream stream = serializer.getOutput();
            stream.i32(sharedLineNos.size());
            for (short line : sharedLineNos)
                stream.i16(line);
        }
        else
        {
            MemoryInputStream stream = serializer.getInput();
            int count = stream.i32();
            sharedLineNos = new ArrayList<>(count);
            for (int i = 0; i < count; ++i)
                sharedLineNos.add(stream.i16());
        }

        sharedLocalVariables = serializer.arraylist(sharedLocalVariables,
            LocalVariableDefinitionRow.class);

        if (serializer.isWriting())
        {
            MemoryOutputStream stream = serializer.getOutput();

            {
                int[] stringAIndices = new int[stringATable.size()];
                int offset = 0;
                for (int i = 0; i < stringAIndices.length; ++i)
                {
                    String value = stringATable.get(i);
                    stringAIndices[i] = offset;
                    offset += value.length() + 1;
                }
                serializer.intvector(stringAIndices);
                stream.i32(offset);
                for (String string : stringATable)
                    stream.str(string, string.length() + 1);
            }

            {
                int[] stringWIndices = new int[stringWTable.size()];
                int offset = 0;
                for (int i = 0; i < stringWIndices.length; ++i)
                {
                    String value = stringWTable.get(i);
                    stringWIndices[i] = offset;
                    offset += value.length() + 1;
                }
                serializer.intvector(stringWIndices);
                stream.i32(offset);
                for (String string : stringWTable)
                    stream.wstr(string, string.length() + 1);
            }

        }
        else
        {
            MemoryInputStream stream = serializer.getInput();

            int[] stringAIndices = serializer.intvector(null);
            byte[] stringATable = stream.bytearray();

            for (int i = 0; i < stringAIndices.length; ++i)
            {
                int start = stringAIndices[i];
                int end = ((i + 1) >= stringAIndices.length) ? stringATable.length :
                    stringAIndices[i + 1];
                this.stringATable.add(
                    new String(Arrays.copyOfRange(stringATable, start, end),
                        StandardCharsets.US_ASCII)
                        .replace("\0", "")
                );
            }

            int[] stringWIndices = serializer.intvector(null);
            int stringWSize = stream.i32();
            byte[] stringWTable = stream.bytes(stringWSize * 2);

            for (int i = 0; i < stringWIndices.length; ++i)
            {
                int start = stringWIndices[i] * 2;
                int end = ((i + 1) >= stringWIndices.length) ? stringWTable.length :
                    stringWIndices[i + 1] * 2;
                this.stringWTable.add(
                    new String(Arrays.copyOfRange(stringWTable, start, end),
                        StandardCharsets.UTF_16BE)
                        .replace("\0", "")
                );
            }
        }

        if (version >= 0x3e2)
        {
            if (serializer.isWriting())
            {
                long[] output = new long[constantTableS64.size()];
                for (int i = 0; i < output.length; ++i)
                    output[i] = constantTableS64.get(i);
                serializer.longvector(output);
            }
            else
            {
                long[] constants = serializer.longvector(null);
                if (constants != null)
                {
                    constantTableS64 = new ArrayList<>(constants.length);
                    for (int i = 0; i < constants.length; ++i)
                        constantTableS64.add(constants[i]);
                }
            }
        }

        if (serializer.isWriting())
        {
            MemoryOutputStream stream = serializer.getOutput();
            stream.i32(constantTable.size());
            for (float constant : constantTable)
                stream.f32(constant);
        }
        else
        {
            MemoryInputStream stream = serializer.getInput();
            int count = stream.i32();
            constantTable = new ArrayList<>(count);
            for (int i = 0; i < count; ++i)
                constantTable.add(stream.f32());
        }

        if (version >= 0x1ec)
        {
            if (serializer.isWriting())
            {
                MemoryOutputStream stream = serializer.getOutput();
                stream.i32(dependingGUIDs.size());
                for (GUID guid : dependingGUIDs)
                    stream.guid(guid);
            }
            else
            {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                dependingGUIDs = new ArrayList<>(count);
                for (int i = 0; i < count; ++i)
                    dependingGUIDs.add(stream.guid());
            }
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        return size + 2_000_000;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
        serializer.struct(this, RScript.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.SCRIPT,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }
}
