package core.device.model.security;

import core.Singleton;
import core.device.model.AddressType;
import core.device.model.Device;
import packet.model.Packet;
import packet.model.PacketType;
import packet.model.Payload;
import topology.modification.ConnectionUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

// defines the methods and protocols for pairing and key distribution,
// the corresponding security toolbox, and the Security Manager Protocol (SMP),
// which defines the pairing command frame format, frame structure and timeout restriction.
public class SecurityManager {
    private Mode mode;

    // Pairing is performed to establish keys which can then be used to encrypt a link.
    // A transport specific key distribution is then performed to share the keys.
    public static void Pair(Device deviceFrom, Device deviceTo, boolean oob, boolean bondingFlags, boolean mitm, boolean secureConnection, String maxEncrSize) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Packet packetInitiator = Packet.sendPacket(new Packet(Singleton.getTime(), PacketType.LL_CONNECTION_PARAM_REQ, deviceFrom, deviceTo, ConnectionUtil.nextChannel(
                deviceTo.getPacketFactory().getConnectionController().getChannelMap(),
                deviceTo.getPacketFactory().getConnectionController().getHopIncrement()),
                deviceFrom.getDataRate(), getPairPacket(true, deviceFrom.getIOCapabilities(), oob, bondingFlags, mitm, secureConnection, "")));

        Packet packetResponder = Packet.sendPacket(new Packet(Singleton.getTime(), PacketType.LL_CONNECTION_PARAM_RSP, deviceTo, deviceFrom, ConnectionUtil.nextChannel(
                deviceTo.getPacketFactory().getConnectionController().getChannelMap(),
                deviceTo.getPacketFactory().getConnectionController().getHopIncrement()),
                deviceTo.getDataRate(), getPairPacket(false, deviceTo.getIOCapabilities(), oob, bondingFlags, mitm, secureConnection, "")));

        ChooseKeyGenerationMethod(packetInitiator, packetResponder);
    }

    public static void ChooseKeyGenerationMethod(Packet packetInitiator, Packet packetResponder) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if(Boolean.parseBoolean(packetInitiator.getPayload().getCtrData().get("SC"))
                && Boolean.parseBoolean(packetResponder.getPayload().getCtrData().get("SC"))){
            ConnectLESecure(packetInitiator, packetResponder);
        } else {
            LELegacyPair(packetInitiator, packetResponder);
        }
    }

    public static void ConnectLESecure(Packet packetInitiator, Packet packetResponder) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if(Boolean.parseBoolean(packetInitiator.getPayload().getCtrData().get("OOB"))
                || Boolean.parseBoolean(packetResponder.getPayload().getCtrData().get("OOB"))){
            OOB(packetInitiator, packetResponder);
        } else if (!Boolean.parseBoolean(packetInitiator.getPayload().getCtrData().get("MITM"))
                || !Boolean.parseBoolean(packetResponder.getPayload().getCtrData().get("MITM"))) {
            JustWorks(packetInitiator, packetResponder);
        } else  {
            UseIOCapabilities(packetInitiator, packetResponder);
        }
    }

    public static void LELegacyPair(Packet packetInitiator, Packet packetResponder) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if(Boolean.parseBoolean(packetInitiator.getPayload().getCtrData().get("OOB"))
                && Boolean.parseBoolean(packetResponder.getPayload().getCtrData().get("OOB"))){
            OOB(packetInitiator, packetResponder);
        } else if (!Boolean.parseBoolean(packetInitiator.getPayload().getCtrData().get("MITM"))
                || !Boolean.parseBoolean(packetResponder.getPayload().getCtrData().get("MITM"))) {
            JustWorks(packetInitiator, packetResponder);
        } else  {
            UseIOCapabilities(packetInitiator, packetResponder);
        }
    }

    public static void OOB(Packet packetInitiator, Packet packetResponder) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Long mRand = AESUtil.generateRandom();
        Long sRand = AESUtil.generateRandom();
        SecretKey tk = GenerateTemporaryKey("OOB");

        Map<Device.IOCapability, String> IOCapabilitiesCode = Map.ofEntries(
                Map.entry(Device.IOCapability.DISPLAY_ONLY, "0"),
                Map.entry(Device.IOCapability.KEYBOARD_ONLY, "2"),
                Map.entry(Device.IOCapability.NO_INPUT_NO_OUTPUT, "3"),
                Map.entry(Device.IOCapability.KEYBOARD_DISPLAY, "4")
        );

        //TODO OOB will always be 0x01
        long pairReqComm = Long.parseLong("1", 16)
                | Long.parseLong(IOCapabilitiesCode.get(packetResponder.getDeviceFrom().getIOCapabilities()), 16)
                | Long.parseLong(Boolean.parseBoolean(packetResponder.getPayload().getCtrData().get("OOB")) ? "1" : "0", 16);

        long pairRespComm = Long.parseLong("2", 16)
                | Long.parseLong(IOCapabilitiesCode.get(packetResponder.getDeviceFrom().getIOCapabilities()), 16)
                | Long.parseLong(Boolean.parseBoolean(packetResponder.getPayload().getCtrData().get("OOB")) ? "1" : "0", 16);

        long initiatingDeviceAddressType = packetInitiator.getDeviceFrom().getDeviceAddress().getType() == AddressType.PUBLIC ?
                Long.parseLong("0", 16) : Long.parseLong("1");

        long respondingDeviceAddressType = packetResponder.getDeviceFrom().getDeviceAddress().getType() == AddressType.PUBLIC ?
                Long.parseLong("0", 16) : Long.parseLong("1");

        String confirmInitiator = pairConfirm(mRand, tk, pairReqComm, pairRespComm, initiatingDeviceAddressType, packetInitiator.getDeviceFrom().getDeviceAddress().getAddress(), respondingDeviceAddressType,  packetResponder.getDeviceFrom().getDeviceAddress().getAddress());
        sendConfirmPacket(packetInitiator.getDeviceFrom(), packetInitiator.getDeviceTo(), confirmInitiator);

        String confirmResponder = pairConfirm(sRand, tk, pairReqComm, pairRespComm, initiatingDeviceAddressType, packetInitiator.getDeviceFrom().getDeviceAddress().getAddress(), respondingDeviceAddressType,  packetResponder.getDeviceFrom().getDeviceAddress().getAddress());
        sendConfirmPacket(packetResponder.getDeviceFrom(), packetResponder.getDeviceTo(), confirmResponder);

        sendRand(packetInitiator.getDeviceFrom(), packetResponder.getDeviceFrom(), Long.toHexString(mRand), PacketType.LL_CONNECTION_PARAM_REQ);
        sendRand(packetResponder.getDeviceFrom(), packetInitiator.getDeviceFrom(), Long.toHexString(sRand), PacketType.LL_CONNECTION_PARAM_RSP);

        String stk = generateSTK(tk, sRand, mRand, AESUtil.generateIv());

        sendSTK(packetInitiator.getDeviceFrom(), packetResponder.getDeviceFrom(), PacketType.LL_START_ENC_REQ, stk);
        sendSTK(packetResponder.getDeviceFrom(), packetInitiator.getDeviceFrom(), PacketType.LL_START_ENC_RSP, stk);
    }

    public static void UseIOCapabilities(Packet packetInitiator, Packet packetResponder){
        Device initiator = packetInitiator.getDeviceFrom();
        Device responder = packetResponder.getDeviceFrom();
        switch(responder.getIOCapabilities()){
            case DISPLAY_ONLY -> {
                switch (initiator.getIOCapabilities()) {
                    case DISPLAY_ONLY, NO_INPUT_NO_OUTPUT -> JustWorks(packetInitiator, packetResponder);
                    case KEYBOARD_ONLY, KEYBOARD_DISPLAY -> PassKeyEntry(packetInitiator, packetResponder);
                }
            }

            case KEYBOARD_ONLY -> {
                switch (initiator.getIOCapabilities()) {
                    case NO_INPUT_NO_OUTPUT -> JustWorks(packetInitiator, packetResponder);
                    case DISPLAY_ONLY, KEYBOARD_ONLY, KEYBOARD_DISPLAY -> PassKeyEntry(packetInitiator, packetResponder);
                }
            }

            case KEYBOARD_DISPLAY -> {
                switch (initiator.getIOCapabilities()) {
                    case DISPLAY_ONLY -> PassKeyEntry(packetInitiator, packetResponder);
                    case KEYBOARD_ONLY -> PassKeyEntry(packetInitiator, packetResponder);
                    case KEYBOARD_DISPLAY -> PassKeyEntry(packetInitiator, packetResponder);
                    case NO_INPUT_NO_OUTPUT -> JustWorks(packetInitiator, packetResponder);
                }
            }

            case NO_INPUT_NO_OUTPUT -> JustWorks(packetInitiator, packetResponder);
        }
    }

    private static void JustWorks(Packet packetInitiator, Packet packetResponder){

    }

    private static void PassKeyEntry(Packet packetInitiator, Packet packetResponder){

    }

    private void PassKey(String initiator, String responder){
        // if(initiator(display) || responder(display)) {
        //  Generate random pass key [000000; 999999]
        //  The other device must enter with keyboard the number
        //  if(success){
        //      //Generate 128-but numbers for initiator and responder
        //      A = generateMrand(); // initiating device
        //      B = generateSrand(); // responding device
        //      pairConfirm(A, B);
        //  }
        // }
    }

    //https://community.nxp.com/t5/Wireless-Connectivity/Bluetooth-Low-Energy-SMP-Pairing/m-p/376931
    private static String pairConfirm(Long rand, SecretKey temporaryKey, long pairReqComm, long pairRespComm, long initiatingDeviceAddressType, String initiatingDeviceAddress, long respondingDeviceAddressType, String respondingDeviceAddress) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        long iat = initiatingDeviceAddressType << 7;
        long rat = respondingDeviceAddressType << 7;
        long p1 = pairRespComm | pairReqComm | rat | iat;
        long p2 = Long.parseLong(initiatingDeviceAddress.replace("-", ""), 16) | Long.parseLong(respondingDeviceAddress.replace("-", ""), 16);

        IvParameterSpec ivParameterSpec = AESUtil.generateIv();

        return AESUtil.encrypt("", (AESUtil.encrypt("", String.valueOf((rand | p1) | p2), temporaryKey, ivParameterSpec)), temporaryKey, ivParameterSpec);

//        String sConfirm = AESUtil.encrypt("", (AESUtil.encrypt("", String.valueOf((Long.parseLong(sRand.toString(), 16) | p1) | p2), temporaryKey, ivParameterSpec)), temporaryKey, ivParameterSpec);


        //+++ initiator -> responder type of communication


        //+++ Mconfirm | Sconfirm = c1(k, r, pres, preq, iat, ia, rat, ra);
        //  k = TK, r = Mrad | Srand, preq = Pairing req command, pres = Pairing res command, iat = Initiating device address type
        //  ia = Initiating device, rat = Responding device address, ra = Responding device type

        // transmitMConfirm();
        // transmitSConfirm();

        // transmitMRand();

        // if(verifyMconfirm()) {
        //  transmitSRand();
        // }

        // if(verifySConfirm()) {
        //  GenerateSTK();
        // }

    }

    public static void sendConfirmPacket(Device initiator, Device responder, String confirmPayload) {
        Packet.sendPacket(new Packet(Singleton.getTime(),
                PacketType.LL_CONNECTION_PARAM_RSP,
                initiator,
                responder,
                responder.getPacketFactory().getConnectionController() == null ?
                        ConnectionUtil.nextChannel(initiator.getPacketFactory().getConnectionController().getChannelMap(),
                                initiator.getPacketFactory().getConnectionController().getHopIncrement()) :
                        ConnectionUtil.nextChannel(responder.getPacketFactory().getConnectionController().getChannelMap(),
                                responder.getPacketFactory().getConnectionController().getHopIncrement()),
                initiator.getDataRate(),
                new Payload(PacketType.LL_CONNECTION_PARAM_RSP.toString(), Map.ofEntries(
                    Map.entry("confirm", confirmPayload)))
            )
        );
    }

    public static void sendRand(Device initiator, Device responder, String randPayload, PacketType packetType) {
//        System.out.println(initiator.getPacketFactory().getConnectionController());
//        System.out.println(responder.getPacketFactory().getConnectionController());

        Packet.sendPacket(new Packet(Singleton.getTime(),
                        packetType,
                        initiator,
                        responder,
                        responder.getPacketFactory().getConnectionController() == null ?
                            ConnectionUtil.nextChannel(initiator.getPacketFactory().getConnectionController().getChannelMap(),
                                            initiator.getPacketFactory().getConnectionController().getHopIncrement()) :
                                ConnectionUtil.nextChannel(responder.getPacketFactory().getConnectionController().getChannelMap(),
                                        responder.getPacketFactory().getConnectionController().getHopIncrement()),
                        initiator.getDataRate(),
                        new Payload(packetType.toString(), Map.ofEntries(
                                Map.entry("rand", randPayload)))
                )
        );
    }

    private static SecretKey GenerateTemporaryKey(String method) throws NoSuchAlgorithmException {
        switch (method) {
            case "JustWorks" -> {return AESUtil.generateKey(0);}
            case "PassKey" -> {return AESUtil.generateKey(19);}
            case "OOB" -> {return AESUtil.generateKey(128);}
            default -> {return null;}
        }
    }

    private static void sendSTK(Device initiator, Device responder, PacketType packetType, String stk) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
//        String stk = generateSTK(tk, sRand, mRand, AESUtil.generateIv());
        Packet.sendPacket(new Packet(Singleton.getTime(),
                        packetType,
                        initiator,
                        responder,
                        responder.getPacketFactory().getConnectionController() == null ?
                                ConnectionUtil.nextChannel(initiator.getPacketFactory().getConnectionController().getChannelMap(),
                                        initiator.getPacketFactory().getConnectionController().getHopIncrement()) :
                                ConnectionUtil.nextChannel(responder.getPacketFactory().getConnectionController().getChannelMap(),
                                        responder.getPacketFactory().getConnectionController().getHopIncrement()),
                        initiator.getDataRate(),
                        new Payload(packetType.toString(), Map.ofEntries(
                                Map.entry("STK", stk)))
                )
        );
    }

    private static String generateSTK(SecretKey tk, Long sRand, Long mRand, IvParameterSpec iv) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        long r = sRand & mRand;
        return AESUtil.encrypt("AES/CBC/PKCS5Padding", String.valueOf(r), tk, iv);
    }

    private static Payload getPairPacket(boolean initiator, Device.IOCapability IOCapabilities, boolean oob, boolean bondingFlags, boolean mitm,
                                      boolean secureConnection, String maxEncrSize){
        return new Payload(PacketType.LL_CONNECTION_PARAM_REQ.toString(), Map.ofEntries(
                Map.entry("Code", initiator ? "Pairing Request" : "Pairing Response"), //0x01 Pairing Request, 0x02 Pairing Response
                Map.entry("IO Cap", IOCapabilities.toString()), //0x00 Display only, 0x01 Display yes/no, 0x02 Keyboard only, 0x03 No IO, 0x04 Keyboard Display, 0x05-0xFF Reserved
                Map.entry("OOB", String.valueOf(oob)), // 0x00 OOB not present, 0x01 OOB present
                Map.entry("BF", String.valueOf(bondingFlags)), //00 No bonding, 01 Bonding, 10 + 11 Reserved
                Map.entry("MITM", String.valueOf(mitm)), // Request True/False for MITM protection
                Map.entry("SC", String.valueOf(secureConnection)), // Request True/False for Secure Connection
                Map.entry("Maximum encryption size", maxEncrSize) // [7, 16] octets
//                Map.entry("Initiator Key Distribution", ""),
//                Map.entry("Responder Key Distribution", "")
        ));
    }
}
