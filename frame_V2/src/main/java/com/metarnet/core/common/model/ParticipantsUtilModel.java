package com.metarnet.core.common.model;

import com.unicom.ucloud.workflow.objects.Participant;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: song
 * Date: 13-4-1
 * Time: 上午11:38
 * To change this template use File | Settings | File Templates.
 */
public class ParticipantsUtilModel {
    private List<Participant>  participantList;

    public List<Participant> getParticipantList() {
        return participantList;
    }

    public void setParticipantList(List<Participant> participantList) {
        this.participantList = participantList;
    }
}
