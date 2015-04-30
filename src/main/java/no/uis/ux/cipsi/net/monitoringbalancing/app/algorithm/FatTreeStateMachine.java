package no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;

public class FatTreeStateMachine {
    /*
     *              ----------------------------
     *              |                          |
     *              v                          |
     *            start                        |
     *      |    |     |     |                 |
     *      v    v     v     v                 |
     *      h1 -> e1 -> a1 -> c -> a2 -> e2 -> h2
     *            |     |          ^           ^
     *            |     |          |           |
     *            |     ------------           |
     *            |                            |
     *            ------------------------------
     */

    public FatTreeStateMachine() {
        // TODO Auto-generated constructor stub
    }
    public FatTreeStateMachine(FatTreeStateMachine sm) {
        switch (sm.state) {
        case H1:
            this.state = State.H1;
            break;
        case E1:
            this.state = State.E1;
            break;
        case A1:
            this.state = State.A1;
            break;
        case C:
            this.state = State.C;
            break;
        case A2:
            this.state = State.A2;
            break;
        case E2:
            this.state = State.E2;
            break;
        case H2:
            this.state = State.H2;
            break;
        case START:
            this.state = State.START;
            break;

        default:
            break;
        }
    }
    enum State {
        START, H1, E1, A1, C, A2, E2, H2

    }
    State state = State.START;

    enum Move {
        H, E, A, C
    }
    Move lastMove = null;

    private Move getMove(Node neighbor) {
        Move nextMove = null;
        if (neighbor instanceof Switch){
            switch (((Switch) neighbor).getType()) {
            case CORE:
                nextMove = Move.C;
                break;
            case AGGREGATION:
                nextMove = Move.A;
                break;
            case EDGE:
                nextMove = Move.E;
                break;
            default:
                break;
            }
        } else if (neighbor instanceof Host) {
            nextMove = Move.H;
        }
        return nextMove;
    }

    public void resetState(State state) {
        this.state = state;
    }

    public void moveState(Node neighbor) throws Exception {
        Move nextMove = getMove(neighbor);
        State oldState = state;
        switch (state) {
        case START:
            switch (nextMove) {
            case C:
                state = State.C;
                break;
            case A:
                state = State.A1;
                break;
            case E:
                state = State.E1;
                break;
            case H:
                state = State.H1;
                break;

            default:
                throw new Exception("Illegal Move");
            }
            break;

        case H1:
            switch (nextMove) {
            case E:
                state = State.E1;
                break;

            default:
                throw new Exception("Illegal Move");
            }
            break;

        case E1:
            switch (nextMove) {
            case A:
                state = State.A1;
                break;
            case H:
                state = State.H2;
                break;

            default:
                throw new Exception("Illegal Move");
            }
            break;

        case A1:
            switch (nextMove) {
            case C:
                state = State.C;
                break;
            case E:
                state = State.E2;
                break;
            default:
                throw new Exception("Illegal Move");
            }
            break;

        case C:
            switch (nextMove) {
            case A:
                state = State.A2;
                break;
            default:
                throw new Exception("Illegal Move");
            }
            break;

        case A2:
            switch (nextMove) {
            case E:
                state = State.E2;
                break;
            default:
                throw new Exception("Illegal Move");
            }
            break;

        case E2:
            switch (nextMove) {
            case H:
                state = State.H2;
                break;
            default:
                throw new Exception("Illegal Move");
            }
            break;

        case H2:
            switch (nextMove) {
            default:
                throw new Exception("Illegal Move");
            }

        default:
            break;
        }
        lastMove = nextMove;
        System.out.println("Change State: " + oldState + " --" + nextMove + "--> " + state);
    }

    public boolean isValidMove(Node neighbor) {
        Move move = getMove(neighbor);
        boolean valid = false;

        switch (state) {
        case START:
            switch (move) {
            case C:
                valid = true;
                break;
            case A:
                valid = true;
                break;
            case E:
                valid = true;
                break;
            case H:
                valid = true;
                break;

            default:
                valid = false;
            }
            break;

        case H1:
            switch (move) {
            case E:
                valid = true;
                break;

            default:
                valid = false;
            }
            break;

        case E1:
            switch (move) {
            case A:
                valid = true;
                break;
            case H:
                valid = true;
                break;
            default:
                valid = false;
            }
            break;

        case A1:
            switch (move) {
            case C:
                valid = true;
                break;
            case E:
                valid = true;
                break;
            default:
                valid = false;
            }
            break;

        case C:
            switch (move) {
            case A:
                valid = true;
                break;
            default:
                valid = false;
            }
            break;

        case A2:
            switch (move) {
            case E:
                valid = true;
                break;
            default:
                valid = false;
            }
            break;

        case E2:
            switch (move) {
            case H:
                valid = true;
                break;
            default:
                valid = false;
            }
            break;

        case H2:
            switch (move) {
            default:
                valid = false;
            }

        default:
            valid = false;
            break;
        }


        return valid;
    }
    @Override
    public String toString() {
        return "LastMove: " + lastMove + ", State: " + state;
    }
}
