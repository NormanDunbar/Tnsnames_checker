//--------------------------------------------------------------------
// The MIT License (MIT) 
// 
// Copyright (c) 2014 by Norman Dunbar 
// 
// Permission is hereby granted, free of charge, to any person 
// obtaining a copy of this software and associated documentation 
// files (the "Software"), to deal in the Software without 
// restriction, including without limitation the rights to use, 
// copy, modify, merge, publish, distribute, sublicense, and/or sell 
// copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following 
// conditions: 
//
// The above copyright notice and this permission notice shall be 
// included in all copies or substantial portions of the Software. 
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
// OTHER DEALINGS IN THE SOFTWARE. 
//
// Project      : Oracle Tnsnames.ora parser.
// Developed by : Norman Dunbar, norman@dunbar-it.co.uk
//--------------------------------------------------------------------

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import java.io.File;
import java.util.*;

public class tnsnamesInterfaceListener extends tnsnamesParserBaseListener
{
    tnsnamesParser parser;                  // The Parser we are listening for.
    String[] ruleNames;                     // The parser's list of rule names.
    List<String> aliasNames = new ArrayList<String>();
    List<String> duplicateNames = new ArrayList<String>();

    int lineNumber;                         // Which line are we parsing?
    int charPosition;                       // And where on the line?
    String whereAmI;                        // Location in file message.
    int totalWarnings = 0;                  // Total count of WARNINGs in this file.
    int totalErrors = 0;                    // Total count of ERRORs in this file.
    int totalInfo = 0;                      // Total count of INFOs in this file.
    int thisWarnings = 0;                   // Total count of WARNINGs in this entry.
    int thisErrors = 0;                     // Total count of ERRORs in this entry.
    int thisInfo = 0;                       // Total count of INFO in this entry.
    int totalDuplicates = 0;                // Were there any duplicate entries found?

    
    // Some counters, these will be zero'd on entry to a tns_alias and
    // incremented for each different parameter found in an entry.

    // The following are all to be found in the DESCRIPTION.
    LineNumber firstSduLine = new LineNumber(0);                // SDU.
    LineNumber firstLoadBalanceLine = new LineNumber(0);        // LOAD_BALANCE.
    LineNumber firstEnableLine = new LineNumber(0);             // ENABLE.
    LineNumber firstFailOverLine = new LineNumber(0);           // FAILOVER.
    LineNumber firstRecvBufLine = new LineNumber(0);            // RECV_BUF_SIZE
    LineNumber firstSendBufLine = new LineNumber(0);            // SEND_BUF_SIZE
    LineNumber firstSourceRouteLine = new LineNumber(0);        // SOURCE_ROUTE
    LineNumber firstServiceTypeLine = new LineNumber(0);        // SERVICE_TYPE
    LineNumber firstSecurityLine = new LineNumber(0);           // SECURITY
    LineNumber firstConnTimeoutLine = new LineNumber(0);        // CONN_TIMEOUT
    LineNumber firstRetryCountLine = new LineNumber(0);         // RETRY_COUNT
    LineNumber firstTctLine = new LineNumber(0);                // TCT

    // The following are all DESCRIPTION_LIST parameters.
    LineNumber firstDL_LoadBalanceLine = new LineNumber(0);     // LOAD_BALANCE.
    LineNumber firstDL_FailOverLine = new LineNumber(0);        // FAILOVER.
    LineNumber firstDL_SourceRouteLine = new LineNumber(0);     // SOURCE_ROUTE.

    // The following are all ADDRESS_LIST parameters.
    LineNumber firstAL_LoadBalanceLine = new LineNumber(0);     // LOAD_BALANCE.
    LineNumber firstAL_FailOverLine = new LineNumber(0);        // FAILOVER.
    LineNumber firstAL_SourceRouteLine = new LineNumber(0);     // SOURCE_ROUTE.

    // The following are all ADDRESS parameters.
    LineNumber firstA_RecvBufLine = new LineNumber(0);          // RECV_BUF_SIZE.
    LineNumber firstA_SendBufLine = new LineNumber(0);          // SEND_BUF_SIZE.
    
    // The following are all CONNECT_DATA parameters.
    LineNumber firstCD_ServiceNameLine = new LineNumber(0);     // SERVICE_NAME.
    LineNumber firstCD_SidLine = new LineNumber(0);             // SID.
    LineNumber firstCD_InstanceNameLine = new LineNumber(0);    // INSTANCE_NAME.
    LineNumber firstCD_FailoverModeLine = new LineNumber(0);    // FAILOVER_MODE.
    LineNumber firstCD_GlobalNameLine = new LineNumber(0);      // GLOBAL_NAME.
    LineNumber firstCD_HsLine = new LineNumber(0);              // HS.
    LineNumber firstCD_RdbDatabaseLine = new LineNumber(0);     // RDB_DATABASE.
    LineNumber firstCD_ServerLine = new LineNumber(0);          // SERVER.
    LineNumber firstCD_UrLine = new LineNumber(0);              // UR.

    // These are for TCP Protocol.
    LineNumber firstTCP_HostLine = new LineNumber(0);           // HOST
    LineNumber firstTCP_PortLine = new LineNumber(0);           // PORT
    LineNumber firstTCP_ProtocolLine = new LineNumber(0);       // PROTOCOL

    // These are for IPC Protocol.
    LineNumber firstIPC_KeyLine = new LineNumber(0);            // KEY
    LineNumber firstIPC_ProtocolLine = new LineNumber(0);       // PROTOCOL

    // These are for SPX Protocol.
    LineNumber firstSPX_ServiceLine = new LineNumber(0);        // SERVICE
    LineNumber firstSPX_ProtocolLine = new LineNumber(0);       // PROTOCOL

    // These are for NMP Protocol
    LineNumber firstNMP_PipeLine = new LineNumber(0);           // PIPE
    LineNumber firstNMP_ServerLine = new LineNumber(0);         // SERVER
    LineNumber firstNMP_ProtocolLine = new LineNumber(0);       // PROTOCOL

    // These are for BEQ Protocol
    LineNumber firstBEQ_ProgramLine = new LineNumber(0);        // PROGRAM
    LineNumber firstBEQ_ProtocolLine = new LineNumber(0);       // PROTOCOL
    LineNumber firstBEQ_Argv0Line = new LineNumber(0);          // ARGV0 (ARGV<zero>)
    LineNumber firstBEQ_ArgsLine = new LineNumber(0);           // ARGS

    // These are from BEQ Args.
    LineNumber firstBAD_AddressLine = new LineNumber(0);        // ADDRESS
    LineNumber firstBAD_LocalLine = new LineNumber(0);          // LOCAL
    LineNumber firstBAD_ProtocolLine = new LineNumber(0);       // PROTOCOL

    // These are for the FAILOVER_MODE in CONNECT_DATA.
    LineNumber firstFOM_BackupLine = new LineNumber(0);         // BACKUP
    LineNumber firstFOM_TypeLine = new LineNumber(0);           // TYPE
    LineNumber firstFOM_MethodLine = new LineNumber(0);         // METHOD
    LineNumber firstFOM_RetriesLine = new LineNumber(0);        // RETRIES
    LineNumber firstFOM_DelayLine = new LineNumber(0);          // DELAY

    //---------------------------------------------------------------- 
    // CONSTRUCTOR
    //---------------------------------------------------------------- 
    // We use the parser, because we need the rule names later when we
    // are checking for parameter redefinition of the parameters that
    // apply to DESCRIPTION_LIST, DESCRIPTION and ADDRESS_LIST.
    //---------------------------------------------------------------- 
    public tnsnamesInterfaceListener(tnsnamesParser parser) 
    {
        this.parser = parser;
        this.ruleNames = parser.getRuleNames();
    }


    //---------------------------------------------------------------- 
    // Write an INFO to stderr and increment the information counters.
    //---------------------------------------------------------------- 
    public void doInfo(String message)
    {
        System.err.println(whereAmI + "INFO: " + message);
        totalInfo += 1;
        thisInfo += 1;
    }

    //---------------------------------------------------------------- 
    // Write an WARNING to stderr and increment the warning counters.
    //---------------------------------------------------------------- 
    public void doWarning(String message)
    {
        System.err.println(whereAmI + "WARNING: " + message);
        totalWarnings += 1;
        thisWarnings += 1;
    }

    //---------------------------------------------------------------- 
    // Write an ERROR to stderr and increment the total error counters.
    //---------------------------------------------------------------- 
    public void doError(String message)
    {
        System.err.println(whereAmI + "ERROR: " + message);
        totalErrors += 1;
        thisErrors += 1;
    }

    //---------------------------------------------------------------- 
    // Error Logger for parameter redefinitions
    //---------------------------------------------------------------- 
    // This function simply prints a warning message to stderr when
    // we find a parameter has been redefined at some point.
    //---------------------------------------------------------------- 
    public void tnsRedefined(String tnsSectionName, int firstLineNumber) 
    {
        doWarning(tnsSectionName + " parameter redefines " +
                  tnsSectionName + " parameter at line " + 
                  firstLineNumber + ".");
    }

    //------------------------------------------------------------------
    // Check for parameter redefinitions
    //------------------------------------------------------------------
    // This function checks to see if the passed LineNumber has been
    // redefined by the current lineNumber. If so, prints a warning
    // message and saves the current lineNumber.
    //------------------------------------------------------------------
    // First time we hit a specific parameter, previous will be zero
    // so we save the current line number as the previous one. All 
    // other passes through will be redefinitions and result in warnings.
    //------------------------------------------------------------------
    // And don;t talk to me about Java's inability to properly pass by
    // reference. Having to wrap an int in a class, with a getter and 
    // setter just to be able to swap two ints in a function, is a joke.
    //------------------------------------------------------------------
    public void checkForRedefinition(String message, LineNumber previous, int current)
    {
        if (previous.intValue() != 0)
        {
            // Not the first time we've seen this parameter, so  
            // warn the user about a redefinition.
            tnsRedefined(message, previous.intValue());
        } else {
            // First time we've seen this parameter, so save the 
            // current line number.
            previous.setValue(current);
        }
    }



    //---------------------------------------------------------------- 
    // EVERY RULE
    //---------------------------------------------------------------- 
    // As we enter every rule, extract the line and column positions.
    // Use them to build a location string for the start of this rule.
    //---------------------------------------------------------------- 
    @Override
    public void enterEveryRule(ParserRuleContext ctx)
    {
        Token startToken = ctx.getStart();
        if (startToken != null) 
        {
            // Lines number from 1.
            this.lineNumber = startToken.getLine();

            // Characters from zero. Adjust.
            this.charPosition = 1 + startToken.getCharPositionInLine();
        } 
        else 
        {
            // Just in case Token can ever be null.
            this.lineNumber = 0;
            this.charPosition = 0;
        }

        // Build a location string for error messages etc.
        this.whereAmI = "\tLine "  + lineNumber + ":" + charPosition + " ";
    }

    //---------------------------------------------------------------- 
    // LISTENER ENTRY
    //---------------------------------------------------------------- 
    // We have found a listener style tns_entry.
    //---------------------------------------------------------------- 
    @Override
    public void enterLsnr_entry(tnsnamesParser.Lsnr_entryContext ctx)
    {
        String thisAlias = ctx.alias().getText();

        System.err.println("\n" + whereAmI.substring(1) + " Listener alias found: " + 
                           thisAlias);
        thisInfo = 0;
        thisWarnings = 0;
        thisErrors = 0;

        // Has this alias been seen before in this file?
        if (aliasNames.contains(thisAlias))
        {
            doError("Duplicate alias - " + thisAlias);
            duplicateNames.add(thisAlias);
            totalDuplicates++;
        } else
          {
            // New alias, add it to the list of found aliases.
            aliasNames.add(ctx.alias().getText());
          }
    }

    //---------------------------------------------------------------- 
    // LISTENER EXIT
    //---------------------------------------------------------------- 
    @Override
    public void exitLsnr_entry(tnsnamesParser.Lsnr_entryContext ctx)
    {
        System.err.println("*** INFO: " + thisInfo + ", WARNING: " + 
                           thisWarnings + ", ERRORS: " + thisErrors);
    }


    //---------------------------------------------------------------- 
    // TNS ENTRY
    //---------------------------------------------------------------- 
    // We have found a proper database style tns_entry.
    //---------------------------------------------------------------- 
    @Override
    public void enterTns_entry(tnsnamesParser.Tns_entryContext ctx)
    {
        String thisAlias = ctx.alias_list().getText();
        System.err.println("\n" + whereAmI.substring(1) + " Database alias found: " + 
                           thisAlias);

        // Initialise the counters for this tns alias.
        thisInfo = 0;
        thisWarnings = 0;
        thisErrors = 0;

        // Add each alias to the found aliases list, if it hasn't already been added.
        for (int i = 0; i < ctx.alias_list().alias().size(); i++)
        {
            thisAlias = ctx.alias_list().alias(i).getText();
        
            // Has this alias been seen before in this file?
            if (aliasNames.contains(thisAlias))
            {
                doError("Duplicate alias - " + thisAlias);
                duplicateNames.add(thisAlias);
                totalDuplicates++;
            } else
              {
                aliasNames.add(thisAlias);
              }
        }
    }

    //---------------------------------------------------------------- 
    // TNS EXIT
    //---------------------------------------------------------------- 
    @Override
    public void exitTns_entry(tnsnamesParser.Tns_entryContext ctx)
    {
        System.err.println("*** INFO: " + thisInfo + ", WARNING: " + 
                           thisWarnings + ", ERRORS: " + thisErrors);
    }


    //---------------------------------------------------------------- 
    // DESCRIPTION_LIST ENTRY
    //---------------------------------------------------------------- 
    @Override
    public void enterDescription_list(tnsnamesParser.Description_listContext ctx)
    {
        // Zeroise the first line counters for the parameters applicable
        // to a DESCRIPTION_LIST entry. 
        firstDL_LoadBalanceLine.setValue(0);        // LOAD_BALANCE
        firstDL_FailOverLine.setValue(0);           // FAILOVER
        firstDL_SourceRouteLine.setValue(0);        // SOURCE_ROUTE
    }

    //---------------------------------------------------------------- 
    // DESCRIPTION ENTRY
    //---------------------------------------------------------------- 
    @Override
    public void enterDescription(tnsnamesParser.DescriptionContext ctx)
    {
        // Zeroise the first line counters for the parameters applicable
        // to a DESCRIPTION entry. 
        firstSduLine.setValue(0);                   // SDU
        firstLoadBalanceLine.setValue(0);           // LOAD_BALANCE
        firstEnableLine.setValue(0);                // ENABLE
        firstFailOverLine.setValue(0);              // FAILOVER
        firstRecvBufLine.setValue(0);               // RECV_BUF_SIZE
        firstSendBufLine.setValue(0);               // SEND_BUF_SIZE
        firstSourceRouteLine.setValue(0);           // ROUTE
        firstServiceTypeLine.setValue(0);           // SERVICE_TYPE
        firstSecurityLine.setValue(0);              // SECURITY
        firstConnTimeoutLine.setValue(0);           // CONN_TIMEOUT
        firstRetryCountLine.setValue(0);            // RETRY_COUNT
        firstTctLine.setValue(0);                   // TCT

        // Multiple ADDRESSes without an ADDRESS_LIST?
        int addressCount = ctx.address().size();

        if (ctx.address_list() == null && addressCount > 1) 
        {
            // We might have a problem, but Oracle allows this form anyway.
            doWarning("Missing ADDRESS_LIST, " + 
                      addressCount + " ADDRESS entries found.");
        }

        // Missing CONNECT_DATA?
        if (ctx.connect_data() == null) 
        {
            doError("Missing CONNECT_DATA.");
        }
    }

    //---------------------------------------------------------------- 
    // ADDRESS_LIST ENTRY
    //---------------------------------------------------------------- 
    @Override
    public void enterAddress_list(tnsnamesParser.Address_listContext ctx)
    {
        // Zeroise the first line counters for the parameters applicable
        // to a ADDRESS_LIST entry. 
        firstA_RecvBufLine.setValue(0);             // RECV_BUF_SIZE
        firstA_SendBufLine.setValue(0);             // SEND_BUF_SIZE
    }

    //---------------------------------------------------------------- 
    // ADDRESS ENTRY
    //---------------------------------------------------------------- 
    @Override
    public void enterAddress(tnsnamesParser.AddressContext ctx)
    {
        // Zeroise the first line counters for the parameters applicable
        // to a ADDRESS entry. 
        firstAL_LoadBalanceLine.setValue(0);        // Address List
        firstAL_FailOverLine.setValue(0);
        firstAL_SourceRouteLine.setValue(0);
        firstTCP_HostLine.setValue(0);              // TCP
        firstTCP_PortLine.setValue(0);
        firstTCP_ProtocolLine.setValue(0);
        firstIPC_KeyLine.setValue(0);               // IPC
        firstIPC_ProtocolLine.setValue(0);
        firstSPX_ServiceLine.setValue(0);           // SPX
        firstSPX_ProtocolLine.setValue(0);
        firstNMP_ServerLine.setValue(0);            // NMP
        firstNMP_PipeLine.setValue(0);
        firstNMP_ProtocolLine.setValue(0);
        firstBEQ_ProgramLine.setValue(0);           // BEQ
        firstBEQ_ProtocolLine.setValue(0);
        firstBEQ_Argv0Line.setValue(0);
        firstBEQ_ArgsLine.setValue(0);
    }

    //---------------------------------------------------------------- 
    // CONNECT_DATA ENTRY
    //---------------------------------------------------------------- 
    @Override
    public void enterConnect_data(tnsnamesParser.Connect_dataContext ctx)
    {
        // Zeroise the first line counters for the parameters applicable
        // to a CONNECT_DATA entry. 
        firstCD_ServiceNameLine.setValue(0);        // SERVICE_NAME
        firstCD_SidLine.setValue(0);                // SID
        firstCD_InstanceNameLine.setValue(0);       // INSTANCE_NAME
        firstCD_FailoverModeLine.setValue(0);       // FAILOVER_MODE
        firstCD_GlobalNameLine.setValue(0);         // GLOBAL_NAME
        firstCD_HsLine.setValue(0);                 // HS
        firstCD_RdbDatabaseLine.setValue(0);        // RDB_DATABASE
        firstCD_ServerLine.setValue(0);             // SERVER
        firstCD_UrLine.setValue(0);                 // UR
    }

    //---------------------------------------------------------------- 
    // BA_DESCRIPTION ENTRY
    //---------------------------------------------------------------- 
    @Override
    public void enterBa_description(tnsnamesParser.Ba_descriptionContext ctx)
    {
        // Zeroise the first line counters for the parameters applicable
        // to a BEQ ARGS DESCRIPTION entry. 
        firstBAD_AddressLine.setValue(0);           // ADDRESS
        firstBAD_LocalLine.setValue(0);             // LOCAL
        firstBAD_ProtocolLine.setValue(0);          // PROTOCOL
    }


    //---------------------------------------------------------------- 
    // SERVICE_NAME ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterCd_service_name(tnsnamesParser.Cd_service_nameContext ctx)
    {
        checkForRedefinition("SERVICE_NAME", firstCD_ServiceNameLine, this.lineNumber);
    }

    //---------------------------------------------------------------- 
    // SID ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterCd_sid(tnsnamesParser.Cd_sidContext ctx)
    {
        checkForRedefinition("SID", firstCD_SidLine, this.lineNumber);
    }

    //---------------------------------------------------------------- 
    // INSTANCE_NAME ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterCd_instance_name(tnsnamesParser.Cd_instance_nameContext ctx)
    {
        checkForRedefinition("INSTANCE_NAME", firstCD_InstanceNameLine, this.lineNumber);
    }

    //---------------------------------------------------------------- 
    // FAILOVER_MODE ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition. There will be checks
    // later on for the individual parts of a FAILOVER_MODE.
    //---------------------------------------------------------------- 
    @Override
    public void enterCd_failover_mode(tnsnamesParser.Cd_failover_modeContext ctx)
    {
        checkForRedefinition("FAILOVER_MODE", firstCD_FailoverModeLine, this.lineNumber);

        // Reset the line numbers for the various FAILOVER_MODE parameters.
        firstFOM_BackupLine.setValue(0);
        firstFOM_TypeLine.setValue(0);
        firstFOM_MethodLine.setValue(0);
        firstFOM_RetriesLine.setValue(0);
        firstFOM_DelayLine.setValue(0);
    }

    //---------------------------------------------------------------- 
    // FOM BACKUP ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterFo_backup(tnsnamesParser.Fo_backupContext ctx)
    {
        checkForRedefinition("BACKUP", firstFOM_BackupLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // FOM TYPE ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterFo_type(tnsnamesParser.Fo_typeContext ctx)
    {
        checkForRedefinition("TYPE", firstFOM_TypeLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // FOM METHOD ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterFo_method(tnsnamesParser.Fo_methodContext ctx)
    {
        checkForRedefinition("METHOD", firstFOM_MethodLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // FOM RETRIES ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterFo_retries(tnsnamesParser.Fo_retriesContext ctx)
    {
        checkForRedefinition("RETRIES", firstFOM_RetriesLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // FOM DELAY ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterFo_delay(tnsnamesParser.Fo_delayContext ctx)
    {
        checkForRedefinition("DELAY", firstFOM_DelayLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // GLOBAL_NAME ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterCd_global_name(tnsnamesParser.Cd_global_nameContext ctx)
    {
        checkForRedefinition("GLOBAL_NAME", firstCD_GlobalNameLine, this.lineNumber);
    }

    //---------------------------------------------------------------- 
    // HS ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterCd_hs(tnsnamesParser.Cd_hsContext ctx)
    {
        checkForRedefinition("HS", firstCD_HsLine, this.lineNumber);
    }

    //---------------------------------------------------------------- 
    // RDB_DATABASE ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterCd_rdb_database(tnsnamesParser.Cd_rdb_databaseContext ctx)
    {
        checkForRedefinition("RDB_DATABASE", firstCD_RdbDatabaseLine, this.lineNumber);
    }

    //---------------------------------------------------------------- 
    // SERVER ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterCd_server(tnsnamesParser.Cd_serverContext ctx)
    {
        checkForRedefinition("SERVER", firstCD_ServerLine, this.lineNumber);
    }

    //---------------------------------------------------------------- 
    // UR ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterCd_ur(tnsnamesParser.Cd_urContext ctx)
    {
        checkForRedefinition("UR", firstCD_UrLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // TCP HOST ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterTcp_host(tnsnamesParser.Tcp_hostContext ctx)
    {
        checkForRedefinition("HOST", firstTCP_HostLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // TCP PORT ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterTcp_port(tnsnamesParser.Tcp_portContext ctx)
    {
        checkForRedefinition("PORT", firstTCP_PortLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // TCP PROTOCOL ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterTcp_tcp(tnsnamesParser.Tcp_tcpContext ctx)
    {
        checkForRedefinition("PROTOCOL", firstTCP_ProtocolLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // IPC KEY ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterIpc_key(tnsnamesParser.Ipc_keyContext ctx)
    {
        checkForRedefinition("KEY", firstIPC_KeyLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // IPC PROTOCOL ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterIpc_ipc(tnsnamesParser.Ipc_ipcContext ctx)
    {
        checkForRedefinition("PROTOCOL", firstIPC_ProtocolLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // SPX SERVICE ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterSpx_service(tnsnamesParser.Spx_serviceContext ctx)
    {
        checkForRedefinition("KEY", firstSPX_ServiceLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // SPX PROTOCOL ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterSpx_spx(tnsnamesParser.Spx_spxContext ctx)
    {
        checkForRedefinition("PROTOCOL", firstSPX_ProtocolLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // NMP PIPE ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterNmp_pipe(tnsnamesParser.Nmp_pipeContext ctx)
    {
        checkForRedefinition("PIPE", firstNMP_PipeLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // NMP SERVER ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterNmp_server(tnsnamesParser.Nmp_serverContext ctx)
    {
        checkForRedefinition("SERVER", firstNMP_ServerLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // NMP PROTOCOL ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterNmp_nmp(tnsnamesParser.Nmp_nmpContext ctx)
    {
        checkForRedefinition("PROTOCOL", firstNMP_ProtocolLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // BEQ PROGRAM ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterBeq_program(tnsnamesParser.Beq_programContext ctx)
    {
        checkForRedefinition("PROGRAM", firstBEQ_ProgramLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // BEQ ARGV0 ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterBeq_argv0(tnsnamesParser.Beq_argv0Context ctx)
    {
        checkForRedefinition("ARGV0", firstBEQ_Argv0Line, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // BEQ ARGS ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterBeq_args(tnsnamesParser.Beq_argsContext ctx)
    {
        checkForRedefinition("ARGS", firstBEQ_ArgsLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // BEQ PROTOCOL ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition. However, this parser
    // rule is called from the BEQ protocol and also from the BEQ ARGS
    // so we need to find out who the parent rule is before we check.
    //---------------------------------------------------------------- 
    @Override
    public void enterBeq_beq(tnsnamesParser.Beq_beqContext ctx)
    {
        RuleContext parentCtx = ctx.getParent();
        String parentRule = this.ruleNames[parentCtx.getRuleIndex()];

        // Check for redefinitions. BEQ.
        if (parentRule == "beq_parameter")
        {
            checkForRedefinition("PROTOCOL", firstBEQ_ProtocolLine, this.lineNumber);
        }

        // Check for redefinitions. ARGS.
        if (parentRule == "bad_address")
        {
            checkForRedefinition("PROTOCOL", firstBAD_ProtocolLine, this.lineNumber);
        }
    }


    //---------------------------------------------------------------- 
    // BAD_ADDRESS ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterBad_address(tnsnamesParser.Bad_addressContext ctx)
    {
        checkForRedefinition("ADDRESS", firstBAD_AddressLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // BAD_LOCAL ENTRY
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterBad_local(tnsnamesParser.Bad_localContext ctx)
    {
        checkForRedefinition("LOCAL", firstBAD_LocalLine, this.lineNumber);
    }


    //---------------------------------------------------------------- 
    // PORT (number)
    //---------------------------------------------------------------- 
    // Check the port numbers are 1024 <= n <= 65535
    //---------------------------------------------------------------- 
    @Override
    public void enterPort(tnsnamesParser.PortContext ctx)
    {
        int portNumber = Integer.parseInt(ctx.INT().getText());

        // Below 1024 is suspicious. Requires root privileges to open.
        if (portNumber < 1024) 
        {
            doWarning("Port number, " + portNumber + 
                      " < 1024. May be invalid.");
        } 

        // Above 65535 is out of range.
        if (portNumber > 65535) 
        {
            doError("Port number " + portNumber + 
                    ". Out of range 1024 - 65535.");
        } 
    }

    //---------------------------------------------------------------- 
    // HOST
    //---------------------------------------------------------------- 
    // If this is an IP address type host, we process it. Named hosts
    // cannot be checked at present - we might be able to ping them.
    //
    // There must be 4 "dotted quads" in an IP address (IPv4) so check
    // that this is so. 
    // 
    // Assuming there are 4, the first is 1 <= n <= 254. The remainder
    // are 0 <= n <= 254.
    //---------------------------------------------------------------- 
    @Override
    public void enterHost(tnsnamesParser.HostContext ctx)
    {
        // It is possible that we don't have a host IP.
        if (ctx.IP() == null) 
        {
            return;
        }

        String ipAddress =  ctx.IP().getText();
        if (ipAddress == null) 
        {
            return;
        }

        // split() takes a regex, hence all the escaping!
        // And I have to escape the escape as well!
        String[] dotQuads = ipAddress.split("\\.");

        // We should have got 1.2.3.4 or similar.
        if (dotQuads.length != 4) 
        {
            doError("IP Address '" + ipAddress + 
                    "' malformed. Should be aa.bb.cc.dd");
            return;
        } 

        int minIP = 1;  // Minimum allowed for the first quad.
        for (int eachInt = 0; eachInt < 4; eachInt++) 
        {
            int dotQuad = 0;        // Temp working value.
            String thisQuad = dotQuads[eachInt].toLowerCase(); 

            // We don't worry about non-integers here because the 
            // lexer rule for an IP says QUAD.QUAD.QUAD.QUAD so it cannot fail. :-)
            // Famous last words perhaps ...... but QUADs are decimal, hex or octal numbers.
            // UPDATE: The RFC for an IPv4 address says that there can be
            // Octal or Hex numbers in the quads. 
            // Decimals = 0-254;
            // Octal = 000-376;
            // Hexadecimal = 0x00 - 0xfe (upper or lower case of course)
            // There goes the grammar again! :-(

            int quadLen = thisQuad.length();

            // If the first character is not a zero, it's a full decimal.
            if (thisQuad.charAt(0) != '0') 
            {
                // If the first character is not a zero, it's a full decimal.
                dotQuad = Integer.parseInt(thisQuad);
            } else 
              {
                // If the second character is an 'x' then it's a Hex integer.
                // So the length has to be at least 3 for "0x0", for eg.
                // Did I mention, I have to strip off the '0x' at the start. Sigh.
                if (quadLen > 2 && thisQuad.charAt(1) == 'x') 
                {
                    dotQuad = Integer.parseInt(thisQuad.substring(2), 16);
                } else 
                  {
                    // It cannot be anything but an Octal integer from the leading zero.
                    // UNLESS of course, someone used leading zeros on a decimal like "029".
                    try 
                    {
                        dotQuad = Integer.parseInt(thisQuad, 8);
                    } catch (NumberFormatException x)
                    {
                        // Users eh? Who'd have them! Don't they know that 
                        // leading zeros mean OCTAL! Try to ping 012.02.012.08!
                        dotQuad = Integer.parseInt(thisQuad);
                    }
                  }
              }
                
            if (eachInt == 1) 
            { 
                // Adjust for 1, 2, and 3 which can be zero.
                minIP = 0;      
            }

            if (dotQuad < minIP || dotQuad > 254) 
            {
                doError("dotQuad '" + dotQuads[eachInt] + 
                        "' in '" + ipAddress + "' out of range " + 
                        minIP + " - 254.");
            }
        }
    }

    //---------------------------------------------------------------- 
    // SDU
    //---------------------------------------------------------------- 
    // Check the SDU value is 512 <= n <= 65535.
    // Default is 8192.
    //---------------------------------------------------------------- 
    @Override
    public void enterD_sdu(tnsnamesParser.D_sduContext ctx)
    {
        // If this is the first SDU, firstSduLine will be zero, otherwise
        // it's a redefinition.
        checkForRedefinition("SDU", firstSduLine, this.lineNumber);

        // What is the SDU parameter value?
        int sduValue = Integer.parseInt(ctx.INT().getText());

        // Below 512 is invalid, as is > 65535.
        if (sduValue < 512 || sduValue > 65535) 
        {
            doError("SDU value " + sduValue + 
                    ". Out of range 512 - 65535.");
        } 

        // 8192 is the default setting.
        if (sduValue == 8192) 
        {
            doInfo("SDU value " + sduValue + 
                   ". This is the default setting.");
        } 
    }

    //---------------------------------------------------------------- 
    // LOAD_BALANCE ENTRY on DESCRIPTION_LIST, DESCRIPTION and
    // ADDRESS_LIST.
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterAl_load_balance(tnsnamesParser.Al_load_balanceContext ctx)
    {
        // Who is my parent? We need this to prevent errors when, for example,
        // an ADDRESS_LIST FAIL_OVER parameter is flagged as redefining a parent
        // DESCRIPTION FAIL_OVER parameter, which is not actually the case.
        RuleContext parentCtx = ctx.getParent();

        // We get the rule name, from the grammar, for the parent of this 
        // rule. In lower case as we are dealing with parser rules.
        String parentRule = this.ruleNames[parentCtx.getRuleIndex()];


        // Check for redefinitions, DESCRIPTION_LIST.
        if (parentRule == "dl_parameter")
        {
            checkForRedefinition("LOAD_BALANCE", firstDL_LoadBalanceLine, this.lineNumber);
        }

        // Check for redefinitions, DESCRIPTION.
        if (parentRule == "d_parameter")
        {
            checkForRedefinition("LOAD_BALANCE", firstLoadBalanceLine, this.lineNumber);
        }

        // Check for redefinitions, ADDRESS_LIST.
        if (parentRule == "al_parameter")
        {
            checkForRedefinition("LOAD_BALANCE", firstAL_LoadBalanceLine, this.lineNumber);
        }
    }

    //---------------------------------------------------------------- 
    // ENABLE ENTRY (on DESCRIPTION)
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterD_enable(tnsnamesParser.D_enableContext ctx)
    {
        checkForRedefinition("ENABLE", firstEnableLine, this.lineNumber);
    }

    //---------------------------------------------------------------- 
    // FAILOVER ENTRY on DESCRIPTION_LIST, DESCRIPTION or ADDRESS_LIST.
    //---------------------------------------------------------------- 
    // Check only for parameter redefinition.
    //---------------------------------------------------------------- 
    @Override
    public void enterAl_failover(tnsnamesParser.Al_failoverContext ctx)
    {
        // Who is my parent? We need this top prevent errors when, for example,
        // an ADDRESS_LIST FAIL_OVER parameter is flagged as redefining a parent
        // DESCRIPTION FAIL_OVER parameter, which is not actually the case.
        RuleContext parentCtx = ctx.getParent();

        // We get the rule name, from the grammar, for the parent of this 
        // rule. In lower case as we are dealing with parser rules.
        String parentRule = this.ruleNames[parentCtx.getRuleIndex()];


        // Check for redefinitions, DESCRIPTION_LIST.
        if (parentRule == "dl_parameter")
        {
            checkForRedefinition("FAILOVER", firstDL_FailOverLine, this.lineNumber);
        }

        // Check for redefinitions, DESCRIPTION.
        if (parentRule == "d_parameter")
        {
            checkForRedefinition("FAILOVER", firstFailOverLine, this.lineNumber);
        }

        // Check for redefinitions, ADDRESS_LIST.
        if (parentRule == "al_parameter")
        {
            checkForRedefinition("FAILOVER", firstAL_FailOverLine, this.lineNumber);
        }
    }

    //----------------------------------------------------------------
    //  RECV_BUF ENTRY (on DESCRIPTION or ADDRESS)
    //----------------------------------------------------------------
    // Check only for parameter redefinition.
    //----------------------------------------------------------------
    @Override
    public void enterD_recv_buf(tnsnamesParser.D_recv_bufContext ctx)
    {
        // Who is my parent rule?
        RuleContext parentCtx = ctx.getParent();
        String parentRule = this.ruleNames[parentCtx.getRuleIndex()];

        // Check for redefinitions, DESCRIPTION.
        if (parentRule == "d_parameter")
        {
            checkForRedefinition("RECV_BUF_SIZE", firstRecvBufLine, this.lineNumber);
        }

        // Check for redefinitions, DESCRIPTION.
        if (parentRule == "a_parameter")
        {
            checkForRedefinition("RECV_BUF_SIZE", firstA_RecvBufLine, this.lineNumber);
        }
    }

    //----------------------------------------------------------------
    //  SEND_BUF ENTRY (on DESCRIPTION or ADDRESS)
    //----------------------------------------------------------------
    // Check only for parameter redefinition.
    //----------------------------------------------------------------
    @Override
    public void enterD_send_buf(tnsnamesParser.D_send_bufContext ctx)
    {
        // Who is my parent rule?
        RuleContext parentCtx = ctx.getParent();
        String parentRule = this.ruleNames[parentCtx.getRuleIndex()];

        // Check for redefinitions, DESCRIPTION.
        if (parentRule == "d_parameter")
        {
            checkForRedefinition("SEND_BUF_SIZE", firstSendBufLine, this.lineNumber);
        }

        // Check for redefinitions, DESCRIPTION.
        if (parentRule == "a_parameter")
        {
            checkForRedefinition("SEND_BUF_SIZE", firstA_SendBufLine, this.lineNumber);
        }
    }

    //----------------------------------------------------------------
    //  SOURCE_ROUTE ENTRY on DESCRIPTION_LIST, DESCRIPTION or 
    // ADDRESS_LIST.
    //----------------------------------------------------------------
    // Check only for parameter redefinition.
    //----------------------------------------------------------------
    @Override
    public void enterAl_source_route(tnsnamesParser.Al_source_routeContext ctx)
    {
        // Who is my parent?
        RuleContext parentCtx = ctx.getParent();

        // We get the rule name, from the grammar, for the parent of this 
        // rule. In lower case as we are dealing with parser rules.
        String parentRule = this.ruleNames[parentCtx.getRuleIndex()];


        // Check for redefinitions, DESCRIPTION_LIST.
        if (parentRule == "dl_parameter")
        {
            checkForRedefinition("SOURCE_ROUTE", firstDL_SourceRouteLine, this.lineNumber);
        }

        // Check for redefinitions, DESCRIPTION.
        if (parentRule == "d_parameter")
        {
            checkForRedefinition("SOURCE_ROUTE", firstSourceRouteLine, this.lineNumber);
        }

        // Check for redefinitions, ADDRESS_LIST.
        if (parentRule == "al_parameter")
        {
            checkForRedefinition("SOURCE_ROUTE", firstAL_SourceRouteLine, this.lineNumber);
        }
    }


    //----------------------------------------------------------------
    //  SERVICE_TYPE ENTRY (on DESCRIPTION)
    //----------------------------------------------------------------
    // Check only for parameter redefinition.
    //----------------------------------------------------------------
    @Override
    public void enterD_service_type(tnsnamesParser.D_service_typeContext ctx)
    {
        checkForRedefinition("SOURCE_ROUTE", firstServiceTypeLine, this.lineNumber);
    }

    //----------------------------------------------------------------
    // SECURITY ENTRY (on DESCRIPTION)
    //----------------------------------------------------------------
    // Check only for parameter redefinition.
    //----------------------------------------------------------------
    @Override
    public void enterD_security(tnsnamesParser.D_securityContext ctx)
    {
        // If this is the first SECURITY, firstSecurityLine will be zero, otherwise
        // it's a redefinition.
        checkForRedefinition("SECURITY", firstSecurityLine, this.lineNumber);
    }

    //----------------------------------------------------------------
    // CONNECT_TIMEOUT ENTRY (on DESCRIPTION)
    //----------------------------------------------------------------
    // Check only for parameter redefinition.
    //----------------------------------------------------------------
    @Override
    public void enterD_conn_timeout(tnsnamesParser.D_conn_timeoutContext ctx)
    {
        // If this is the first CONNECT_TIMEOUT, firstConnTimeoutLine will be zero, otherwise
        // it's a redefinition.
        checkForRedefinition("CONNECT_TIMEOUT", firstConnTimeoutLine, this.lineNumber);
    }

    //----------------------------------------------------------------
    // RETRY_COUNT ENTRY (on DESCRIPTION)
    //----------------------------------------------------------------
    // Check only for parameter redefinition.
    //----------------------------------------------------------------
    @Override
    public void enterD_retry_count(tnsnamesParser.D_retry_countContext ctx)
    {
        // If this is the first RETRY_COUNT, firstRetryCountLine will be zero, otherwise
        // it's a redefinition.
        checkForRedefinition("RETRY_COUNT", firstRetryCountLine, this.lineNumber);
    }

    //----------------------------------------------------------------
    // TRANSPORT_CONNECT_TIMEOUT ENTRY (on DESCRIPTION)
    //----------------------------------------------------------------
    // Check only for parameter redefinition.
    //----------------------------------------------------------------
    @Override
    public void enterD_tct(tnsnamesParser.D_tctContext ctx)
    {
        // If this is the first TRANSPORT_CONNECT_TIMEOUT, firstTctLine will be zero, otherwise
        // it's a redefinition.
        checkForRedefinition("TRANSPORT_CONNECT_TIMEOUT", firstTctLine, this.lineNumber);
    }

    //---------------------------------------------------------------- 
    // LSNR_DESCRIPTION ENTRY
    //---------------------------------------------------------------- 
    // Listener DESCRIPTION entry found. Carry out some validation.
    //---------------------------------------------------------------- 
    @Override
    public void enterLsnr_description(tnsnamesParser.Lsnr_descriptionContext ctx)
    {
        // Multiple ADDRESSes without an ADDRESS_LIST?
        int addressCount = ctx.address().size();

        if (ctx.address_list() == null && addressCount > 1) 
        {
            doWarning("Missing ADDRESS_LIST, " + addressCount + 
                      " ADDRESS entries found.");
        }
    }



   //---------------------------------------------------------------- 
    // TNSNAMES EXIT
    //---------------------------------------------------------------- 
    // On exit from the complete tnsnames file, list the run stats and
    // IFILE filenames for further processing. They are not checked by
    // this script automatically (yet!) so need to be done manually.
    //---------------------------------------------------------------- 
    @Override
    public void exitTnsnames(tnsnamesParser.TnsnamesContext ctx)
    {
        // Advise the user of the state of the tnsnames.ora file.
        System.out.println("\n" + String.format("%-80s", "=").replace(" ", "="));
        System.out.println("Parsing Information:");
        System.out.println("===================");
        System.out.println("INFORMATION      : " + totalInfo);
        System.out.println("PARSER WARNINGS  : " + totalWarnings);
        System.out.println("PARSER ERRORS    : " + totalErrors);
        System.out.println("DUPLICATE ENTRIES: " + totalDuplicates);
        for (int i = 0; i < totalDuplicates; i++)
        {
            System.out.println("\tDuplicate[" + i + "] = '" + duplicateNames.get(i) + "'");
        }
        System.out.println(String.format("%-80s", "=").replace(" ", "="));

        // Did we find any IFILE entries? If so, list the files for further analysis.
        if (ctx.ifile() != null)
        {
            int ifileSize = ctx.ifile().size();

            System.out.println("\n" + String.format("%-80s", "-").replace(" ", "-"));
            System.out.println("IFILE List:");
            System.out.println(String.format("%-80s", "-").replace(" ", "-"));

            System.out.println("Please run the tnsnames_checker script on the following " + ifileSize + " files:");

            for (int thisIfile = 0; thisIfile < ifileSize; thisIfile++)
            {
                // Grab the filename including leading and/or trailing (optional) quotes.
                // Plus, whatever crud is between the filename and end of line, if unquoted.
                String ifileFilename = ctx.ifile(thisIfile).I_STRING().getText();

                // This could be double quoted, single quoted or no quotes, so strip off
                // as necessary.
                // Double quotes first:
                if (ifileFilename.startsWith("\""))
                {
                    ifileFilename = ifileFilename.replace("\"", "");
                }

                // Single quotes next:
                // Yes, I could have used an "else", I know. ;-)
                if (ifileFilename.startsWith("'"))
                {
                    ifileFilename = ifileFilename.replace("'", "");
                }

                // If unquoted, we'll have possible leading spaces, plus optional
                // spaces and/or a trailing '\r' & '\n' - get rid.
                // Also cleans any crud from the quoted strings too.
                ifileFilename = ifileFilename.trim();

                System.out.print("\t" + "IFILE[" + thisIfile + "] = '" + ifileFilename + "'");
                
                // Then attempt to determine if it exists. Not necessarily an error if not.
                // It fails to find the file if we leave the (optional) quotes present.
                File inputFile = new File(ifileFilename);
                if (inputFile.isFile()) 
                {
                    // The file exists on this server/laptop/whatever!
                    System.out.println(" (on this computer.)");
                }
                else
                {
                    // The file doesn't exist here. We could be running a check
                    // for another server/laptop/etc so it's not a fatal problem.
                    System.out.println(" (not found on this computer.)");
                }
            }

            System.out.println("End of IFILE List.");
        }
    }
}
