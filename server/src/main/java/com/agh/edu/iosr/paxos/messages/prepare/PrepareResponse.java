package com.agh.edu.iosr.paxos.messages.prepare;

public class PrepareResponse implements Comparable<PrepareResponse> {

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

    public boolean isAnswerPositive() {
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
    public int compareTo(PrepareResponse other) {
        return this.acceptedProposal != null && other.acceptedProposal != null
                ? Long.compare(other.acceptedProposal.getSequenceNumber(), this.acceptedProposal.getSequenceNumber())
                : (this.acceptedProposal == null ? -1 : 1);
    }
}
