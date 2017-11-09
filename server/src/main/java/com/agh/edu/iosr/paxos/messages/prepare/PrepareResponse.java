package com.agh.edu.iosr.paxos.messages.prepare;

public class PrepareResponse {

    private boolean answer;
    private AcceptedProposal acceptedProposal;

    public PrepareResponse() {
    }

    public PrepareResponse(boolean answer) {
        this.answer = answer;
    }

    public PrepareResponse(boolean answer, AcceptedProposal acceptedProposal) {
        this.answer = answer;
        this.acceptedProposal = acceptedProposal;
    }

    public boolean getAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }

    public AcceptedProposal getAcceptedProposal() {
        return acceptedProposal;
    }

    public void setAcceptedProposal(AcceptedProposal acceptedProposal) {
        this.acceptedProposal = acceptedProposal;
    }

    @Override
    public String toString() {
        return "PrepareResponse{" +
                "answer=" + answer +
                ", acceptedProposal=" + acceptedProposal +
                '}';
    }
}
