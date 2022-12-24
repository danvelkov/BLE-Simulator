package core.device.model.security;

// defines the methods and protocols for pairing and key distribution,
// the corresponding security toolbox, and the Security Manager Protocol (SMP),
// which defines the pairing command frame format, frame structure and timeout restriction.
public class SecurityManager {
    private Mode mode;

    // Pairing is performed to establish keys which can then be used to encrypt a link.
    // A transport specific key distribution is then performed to share the keys.
    public static void Pair(PairMethod pairMethod){

    }

    private void GenerateTemporaryKey() {

    }

    public static void Bond(boolean bondable){

    }
}
