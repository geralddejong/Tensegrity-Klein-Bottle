/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

import eu.beautifulcode.eig.math.Arrow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carefully marshall and unmarshall fabrics.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Fablob {
    private static final int MAGIC = 0xFAB00B1E;
    private byte[] bytes;

    public static Fablob read(DataInputStream dis) throws IOException {
        int size = dis.readInt();
        byte[] bytes = new byte[size];
        int offset = 0;
        int read;
        do {
            read = dis.read(bytes, offset, bytes.length - offset);
            offset += read;
        }
        while (offset < bytes.length);
        return new Fablob(bytes);
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    public Fablob(byte[] bytes) {
        this.bytes = bytes;
    }

    public Fablob(Fabric fabric) {
        if (fabric.hasTransformations()) {
            throw new RuntimeException("Cannot create a fablob when transformations are pending");
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);
            out.writeInt(MAGIC);
            packFabric(fabric, out);
            out.close();
            bytes = bos.toByteArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getBytes() {
        return bytes;
    }

    public Fabric createFabric(Thing.Factory factory) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream in = new DataInputStream(bis);
            int magic = in.readInt();
            if (magic != MAGIC) {
                throw new RuntimeException("This is not a fabric!");
            }
            return unpackFabric(in, factory);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void packWho(Who who, DataOutputStream out) throws IOException {
        packShort(who.side.ordinal() + (who.id * 4), out);
    }

    public static Who unpackWho(DataInputStream in) throws IOException {
        int value = unpackShort(in);
        return new Who(Who.Side.values()[value % 4], value / 4);
    }

    public static void packArrow(Arrow arrow, DataOutputStream out) throws IOException {
        packDouble(arrow.x, out);
        packDouble(arrow.y, out);
        packDouble(arrow.z, out);
    }

    public static void unpackArrow(DataInputStream in, Arrow arrow) throws IOException {
        arrow.x = unpackDouble(in);
        arrow.y = unpackDouble(in);
        arrow.z = unpackDouble(in);
    }

    // the rest is private and static

    private static void packFabric(Fabric fabric, DataOutputStream out) throws IOException {
        packLong(fabric.age, out);
        packLong(fabric.lastSpanActive, out);
        for (Who.Side side : Who.Side.values()) {
            packShort(fabric.whoFactory.id[side.ordinal()], out);
        }
        if (fabric.thing != null) {
            packBoolean(true, out);
            fabric.thing.save(out);
        }
        else {
            packBoolean(false, out);
        }
        packShort(fabric.joints.size(), out);
        for (Joint joint : fabric.joints) {
            packJoint(joint, out);
        }
        packShort(fabric.intervals.size(), out);
        for (Interval interval : fabric.intervals) {
            packInterval(interval, out);
        }
        packShort(fabric.faces.size(), out);
        for (Face face : fabric.faces) {
            packFace(face, out);
        }
        packShort(fabric.tetras.size(), out);
        for (Tetra tetra : fabric.tetras) {
            packTetra(tetra, out);
        }
        packShort(fabric.vertebras.size(), out);
        for (Vertebra vertebra : fabric.vertebras) {
            packVertebra(vertebra, out);
        }
    }

    private static Fabric unpackFabric(DataInputStream in, Thing.Factory thingFactory) throws IOException {
        Fabric fabric = new Fabric(thingFactory);
        fabric.age = unpackLong(in);
        fabric.lastSpanActive = unpackLong(in);
        for (Who.Side side : Who.Side.values()) {
            fabric.whoFactory.id[side.ordinal()] = unpackShort(in);
        }
        if (unpackBoolean(in)) {
            fabric.thing = thingFactory.restoreExisting(in, fabric);
        }
        Map<Who, Joint> jointMap = new HashMap<Who, Joint>();
        int jointCount = unpackShort(in);
        while (jointCount-- > 0) {
            Joint joint = unpackJoint(in, thingFactory);
            fabric.joints.add(joint);
            jointMap.put(joint.who, joint);
        }
        int intervalCount = unpackShort(in);
        while (intervalCount-- > 0) {
            fabric.intervals.add(unpackInterval(in, jointMap, thingFactory));
        }
        int faceCount = unpackShort(in);
        while (faceCount-- > 0) {
            fabric.faces.add(unpackFace(fabric, in, jointMap));
        }
        int tetraCount = unpackShort(in);
        while (tetraCount-- > 0) {
            fabric.tetras.add(unpackTetra(in, jointMap));
        }
        int vertebraCount = unpackShort(in);
        while (vertebraCount-- > 0) {
            fabric.vertebras.add(unpackVertebra(in, jointMap));
        }
        return fabric;
    }

    private static void packJoint(Joint joint, DataOutputStream out) throws IOException {
        packWho(joint.who, out);
        packArrow(joint.location, out);
        packArrow(joint.velocity, out);
        packDouble(joint.intervalMass, out);
        if (joint.thing != null) {
            packBoolean(true, out);
            joint.thing.save(out);
        }
        else {
            packBoolean(false, out);
        }
    }

    private static Joint unpackJoint(DataInputStream in, Thing.Factory thingFactory) throws IOException {
        Joint joint = new Joint(unpackWho(in));
        unpackArrow(in, joint.location);
        unpackArrow(in, joint.velocity);
        joint.intervalMass = unpackDouble(in);
        if (unpackBoolean(in)) {
            joint.thing = thingFactory.restoreExisting(in, joint);
        }
        return joint;
    }

    private static void packInterval(Interval interval, DataOutputStream out) throws IOException {
        packRole(interval.role, out);
        packWho(interval.alpha.who, out);
        packWho(interval.omega.who, out);
        packSpan(interval.span, out);
        if (interval.thing != null) {
            packBoolean(true, out);
            interval.thing.save(out);
        }
        else {
            packBoolean(false, out);
        }
    }

    private static Interval unpackInterval(DataInputStream in, Map<Who, Joint> joints, Thing.Factory thingFactory) throws IOException {
        Interval interval = new Interval();
        interval.role = unpackRole(in);
        interval.alpha = joints.get(unpackWho(in));
        interval.omega = joints.get(unpackWho(in));
        interval.span = unpackSpan(in);
        if (unpackBoolean(in)) {
            interval.thing = thingFactory.restoreExisting(in, interval);
        }
        return interval;
    }

    private static void packSpan(Span span, DataOutputStream out) throws IOException {
        packDouble(span.actual, out);
        packDouble(span.ideal, out);
        packDouble(span.stress, out);
        packByte(span.getChainSize(), out);
        Span.Future future = span.future;
        if (future != null) {
            packDouble(future.initial, out);
            packDouble(future.value, out);
            packShort(future.howLong, out);
            packLong(future.when, out);
            future = future.nextFuture;
            while (future != null) {
                packDouble(future.value, out);
                packShort(future.howLong, out);
                future = future.nextFuture;
            }
        }
    }

    private static Span unpackSpan(DataInputStream in) throws IOException {
        Span span = new Span(unpackDouble(in), unpackDouble(in), unpackDouble(in));
        int chainSize = unpackByte(in);
        if (chainSize > 0) {
            Span.Future future = span.future = new Span.Future();
            future.initial = unpackDouble(in);
            future.value = unpackDouble(in);
            future.howLong = unpackShort(in);
            future.when = unpackLong(in);
            while (--chainSize > 0) {
                future = future.nextFuture = new Span.Future();
                future.value = unpackDouble(in);
                future.howLong = unpackShort(in);
            }
        }
        return span;
    }

    private static void packFace(Face face, DataOutputStream out) throws IOException {
        int orientation = face.order.ordinal() + 2 * face.chirality.ordinal();
        packByte(orientation, out);
        packSmallJointList(face.joints, out);
        boolean hasMuscle = face.stressInterval != null;
        packBoolean(hasMuscle, out);
        if (hasMuscle) {
            packWho(face.stressInterval.alpha.who, out);
            packWho(face.stressInterval.omega.who, out);
        }
        boolean hasThing = face.getThing() != null;
        packBoolean(hasThing, out);
        if (hasThing) {
            face.getThing().save(out);
        }
    }

    private static Face unpackFace(Fabric fabric, DataInputStream in, Map<Who, Joint> allJoints) throws IOException {
        int orientation = unpackByte(in);
        Face.Order order = Face.Order.values()[orientation % 2];
        Face.Chirality chirality = Face.Chirality.values()[orientation / 2];
        Face face = new Face(order, chirality);
        unpackSmallJointList(face.joints, in, allJoints);
        if (unpackBoolean(in)) {
            face.setStressInterval(fabric.getInterval(allJoints.get(unpackWho(in)), allJoints.get(unpackWho(in))));
        }
        if (unpackBoolean(in)) {
            face.setThing(fabric.getThingFactory().restoreExisting(in, face));
        }
        return face;
    }

    private static void packTetra(Tetra tetra, DataOutputStream out) throws IOException {
        packSmallJointList(tetra.joints, out);
    }

    private static Tetra unpackTetra(DataInputStream in, Map<Who, Joint> joints) throws IOException {
        Tetra tetra = new Tetra();
        unpackSmallJointList(tetra.joints, in, joints);
        return tetra;
    }

    private static void packVertebra(Vertebra vertebra, DataOutputStream out) throws IOException {
        packSmallJointList(vertebra.joints, out);
    }

    private static Vertebra unpackVertebra(DataInputStream in, Map<Who, Joint> joints) throws IOException {
        Vertebra vertebra = new Vertebra();
        unpackSmallJointList(vertebra.joints, in, joints);
        return vertebra;
    }

    private static void packSmallJointList(List<Joint> joints, DataOutputStream out) throws IOException {
        packByte(joints.size(), out);
        for (Joint joint : joints) {
            packWho(joint.who, out);
        }
    }

    private static void unpackSmallJointList(List<Joint> joints, DataInputStream in, Map<Who, Joint> allJoints) throws IOException {
        int jointCount = unpackByte(in);
        while (jointCount-- > 0) {
            joints.add(allJoints.get(unpackWho(in)));
        }
    }

    // the primitives

    private static void packBoolean(boolean bool, DataOutputStream out) throws IOException {
        out.writeBoolean(bool);
    }

    private static boolean unpackBoolean(DataInputStream in) throws IOException {
        return in.readBoolean();
    }

    private static void packRole(Interval.Role role, DataOutputStream out) throws IOException {
        out.writeByte(role.ordinal());
    }

    private static Interval.Role unpackRole(DataInputStream in) throws IOException {
        return Interval.Role.values()[in.readByte()];
    }

    private static void packByte(int number, DataOutputStream out) throws IOException {
        if (number < Byte.MIN_VALUE || number > Byte.MAX_VALUE) {
            throw new RuntimeException("Couldn't pack byte " + number);
        }
        out.writeShort(number);
    }

    private static int unpackByte(DataInputStream in) throws IOException {
        return in.readShort();
    }

    private static void packShort(int number, DataOutputStream out) throws IOException {
        if (number < Short.MIN_VALUE || number > Short.MAX_VALUE) {
            throw new RuntimeException("Couldn't pack short " + number);
        }
        out.writeShort(number);
    }

    private static int unpackShort(DataInputStream in) throws IOException {
        return in.readShort();
    }

    private static void packDouble(double number, DataOutputStream out) throws IOException {
        out.writeDouble(number);
    }

    private static double unpackDouble(DataInputStream in) throws IOException {
        return in.readDouble();
    }

    private static void packLong(long number, DataOutputStream out) throws IOException {
        out.writeLong(number);
    }

    private static long unpackLong(DataInputStream in) throws IOException {
        return in.readLong();
    }

    private static int packDouble(double number, double low, double high) {
        return Short.MIN_VALUE + (int) Math.round(((Short.MAX_VALUE - Short.MIN_VALUE) * (number - low) / (high - low)));
    }

    private static double unpackDouble(int number, double low, double high) {
        return low + (number - Short.MIN_VALUE) * (high - low) / (Short.MAX_VALUE - Short.MIN_VALUE);
    }

    public static void main(String[] args) {
        double low = -0.390986772717998530;
        double high = 00.390974840566546100;
        double number = -00.297223926604684050;
        int estimate = packDouble(number, low, high);
        double newNumber = unpackDouble(estimate, low, high);
        int newEstimate = packDouble(newNumber, low, high);
        if (newEstimate != estimate) {
            throw new RuntimeException(newEstimate + "!=" + estimate);
        }
        System.out.println("okay");
    }
}