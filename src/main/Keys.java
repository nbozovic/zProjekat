package main;

import gui.controllers.Home;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGInputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import static main.PGPKeyTools.longToBytes;

public class Keys {
    private static final Keys instance = new Keys();

    private static final String secretKeysFileName = "secret.asc";
    private static final String publicKeysFileName = "public.asc";

    private BCPGInputStream publicIn;
    private BCPGInputStream secretIn;

    private ArmoredOutputStream secretOut;
    private ArmoredOutputStream publicOut;

    public PGPPublicKeyRingCollection pgpPublicKeyRingCollection;
    public PGPSecretKeyRingCollection pgpSecretKeyRingCollection;

    private PGPPublicKeyRing publicKeyRing;

    private Keys() {

        try {
            publicIn = new BCPGInputStream(PGPUtil.getDecoderStream(new BufferedInputStream(new FileInputStream(publicKeysFileName))));
            secretIn = new BCPGInputStream(PGPUtil.getDecoderStream(new BufferedInputStream(new FileInputStream(secretKeysFileName))));

            publicKeyRing = new PGPPublicKeyRing(publicIn, new JcaKeyFingerprintCalculator());


            pgpPublicKeyRingCollection = new PGPPublicKeyRingCollection(publicIn, new JcaKeyFingerprintCalculator());
            pgpSecretKeyRingCollection = new PGPSecretKeyRingCollection(secretIn, new JcaKeyFingerprintCalculator());

        } catch (IOException | PGPException e) {
            e.printStackTrace();
        }
    }

    public void testPublicKeyRing(){
        Iterator keyIter = publicKeyRing.getPublicKeys();
        while (keyIter.hasNext()) {
            PGPPublicKey key = (PGPPublicKey) keyIter.next();

            if (key.isEncryptionKey()) {
                PGPKeyTools.printPublicKey(key);
            }
        }
    }
    public void printPublicKeys() {
        Iterator keyRingIter = pgpPublicKeyRingCollection.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();

            Iterator keyIter = keyRing.getPublicKeys();
            while (keyIter.hasNext()) {
                PGPPublicKey key = (PGPPublicKey) keyIter.next();

                if (key.isEncryptionKey()) {
                    PGPKeyTools.printPublicKey(key);
                }
            }
        }
    }

    public ArrayList<Home.PrivateKey> getPrivateKeys(){
        ArrayList<Home.PrivateKey> privateKeys = new ArrayList();

        Iterator keyRingIter = pgpSecretKeyRingCollection.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPSecretKeyRing keyRing = (PGPSecretKeyRing) keyRingIter.next();

            Iterator keyIter = keyRing.getSecretKeys();
            while (keyIter.hasNext()) {
                PGPSecretKey key = (PGPSecretKey) keyIter.next();

                String ownerName = key.getUserIDs().next();
                String keyId = Hex.toHexString(longToBytes(key.getKeyID()));
                boolean masterKey = key.isMasterKey();

                Home.PrivateKey privateKey = new Home.PrivateKey(ownerName, keyId, masterKey);
                privateKeys.add(privateKey);

            }
        }

        return privateKeys;
    }

    public void printPrivateKeys(){
        Iterator keyRingIter = pgpSecretKeyRingCollection.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPSecretKeyRing keyRing = (PGPSecretKeyRing) keyRingIter.next();

            Iterator keyIter = keyRing.getSecretKeys();
            while (keyIter.hasNext()) {
                PGPSecretKey key = (PGPSecretKey) keyIter.next();

                PGPKeyTools.printSecretKey(key);

            }
        }
    }

    public void addPublicKeyRing(PGPPublicKeyRing pgpPublicKeyRing) throws IOException {
        pgpPublicKeyRingCollection = PGPPublicKeyRingCollection.addPublicKeyRing(Keys.getInstance().pgpPublicKeyRingCollection, pgpPublicKeyRing);
        ArmoredOutputStream publicOut = new ArmoredOutputStream (new FileOutputStream("public.asc"));
        pgpPublicKeyRingCollection.encode(publicOut);
        publicOut.close();
    }

    public void addSecretKeyRing(PGPSecretKeyRing pgpSecretKeyRing) throws IOException {
        pgpSecretKeyRingCollection = PGPSecretKeyRingCollection.addSecretKeyRing(Keys.getInstance().pgpSecretKeyRingCollection, pgpSecretKeyRing);
        ArmoredOutputStream secretOut = new ArmoredOutputStream (new FileOutputStream("secret.asc"));
        pgpSecretKeyRingCollection.encode(secretOut);
        secretOut.close();
    }

    public static Keys getInstance() {
        return instance;
    }

    public static void main(String[] args){
        Keys.getInstance().testPublicKeyRing();
    }
}