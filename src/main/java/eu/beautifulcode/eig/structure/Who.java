/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

/**
 * A Who object identifies a joint unambiguously
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class Who implements Comparable<Who> {
    Side side;
    int id;

    Who(Side side, int id) {
        this.side = side;
        this.id = id;
    }

    public Who createOpposite() {
        switch (side) {
            case LEFT: return new Who(Side.RIGHT, id);
            case RIGHT: return new Who(Side.LEFT, id);
            case MIDDLE: return this;
            default : return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Who who = (Who) o;
        return id == who.id && side == who.side;
    }

    @Override
    public int hashCode() {
        int result = side.hashCode();
        result = 31 * result + id;
        return result;
    }

    public String toString() {
        return side.toString()+id;
    }

    public int compareTo(Who who) {
        if (side != who.side) {
            return side.ordinal() - who.side.ordinal();
        }
        return id - who.id;
    }

    public enum Side {
        MIDDLE,
        LEFT,
        RIGHT,
        TEMPORARY,
        ELIMINATED;

        public String toString() {
            return super.toString().substring(0,1);
        }
    }

    public interface Factory {
        Who createMiddle();
        Who createLeft();
        Who createRight();
        Who createTemporary();
        Who createEliminated();
        Who createAnotherLike(Who who);
    }

//    public static void main(String[] args) {
//        System.out.println("negative: "+(new Who(Side.MIDDLE, 8).compareTo(new Who(Side.LEFT,8))));
//        System.out.println("positive: "+(new Who(Side.RIGHT, 8).compareTo(new Who(Side.RIGHT,7))));
//    }

}
