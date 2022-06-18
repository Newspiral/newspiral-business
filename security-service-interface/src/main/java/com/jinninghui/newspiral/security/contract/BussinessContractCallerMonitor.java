package com.jinninghui.newspiral.security.contract;

import com.jinninghui.newspiral.ledger.mgr.contract.BussinessContractConnector;

public interface BussinessContractCallerMonitor extends BussinessContractConnector, Thread.UncaughtExceptionHandler {
}
