package com.pilot.daycare.service;

import com.pilot.daycare.model.LedgerEntry;
import com.pilot.daycare.model.SlotReport;
import com.pilot.daycare.store.InMemoryStore;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReportService {
    private final InMemoryStore store;
    private final LedgerService ledger;

    public ReportService(InMemoryStore store, LedgerService ledger) {
        this.store = store; this.ledger = ledger;
    }

    public SlotReport create(SlotReport r) {
        r.id = UUID.randomUUID().toString();
        store.reports.put(r.id, r);
        return r;
    }

    public void confirm(String reportId) {
        SlotReport r = store.reports.get(reportId);
        if (r == null) return;
        r.confirmations++;
    }

    public void verify(String verifierId, String reportId) {
        SlotReport r = store.reports.get(reportId);
        if (r == null) return;
        r.verifications++;
        ledger.award(verifierId, 1, LedgerEntry.Reason.VERIFY, reportId);
        if (r.verifications >= 2 && r.status == SlotReport.Status.PENDING) {
            r.status = SlotReport.Status.VERIFIED;
            ledger.award(r.reporterId, 2, LedgerEntry.Reason.REPORT, reportId);
        }
    }

    public void flagFake(String reportId) {
        SlotReport r = store.reports.get(reportId);
        if (r == null) return;
        r.flags++;
        if (r.flags >= 3 && r.status != SlotReport.Status.VERIFIED) {
            r.status = SlotReport.Status.REJECTED;
            ledger.award(r.reporterId, -2, LedgerEntry.Reason.FAKE_PENALTY, reportId);
        }
    }
}