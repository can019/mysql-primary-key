package com.github.can019.performance.identifier;

import com.fasterxml.uuid.Generators;

import java.util.UUID;

import static com.github.can019.performance.util.TypeConvertor.hexStringToByte;

public class TimeBasedSequenceIdentifier {

    public static byte[] generate() {
        return generateWithOutHyphenAsByteArray();
    }

    private static String generateWithOutHyphen(){
        UUID uuidV1 = Generators.timeBasedGenerator().generate();
        String[] uuidArr = uuidV1.toString().split("-");

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer
                .append(uuidArr[2])
                .append(uuidArr[1])
                .append(uuidArr[0])
                .append(uuidArr[3])
                .append(uuidArr[4]);

        return stringBuffer.toString();
    }

    private static byte[] generateWithOutHyphenAsByteArray(){
        String uuidWithoutHyphen = generateWithOutHyphen();
        return hexStringToByte(uuidWithoutHyphen);
    }

}
