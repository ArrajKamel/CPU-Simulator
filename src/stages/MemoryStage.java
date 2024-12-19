package stages;

import controller.Simulator;

public class MemoryStage extends Stage{
    // control signals come from the previous pipe
    int memToReg;
    int regWrite;
    int memWrite;
    int branch;

    // values from the previous pipe
    int branchTargetAddress;
    int zero;
    int ALUResult;
    int readData2;
    int writeAddress;

    // values from the current stage to be sent to the next pipe
    int memoryData;

    // PCSrc can be in IF stage or here
    int PCSrc;

    /**
     * Constructs a new stage
     *
     * @param simulator the simulator to which this stage is associated
     */
    public MemoryStage(Simulator simulator) {
        super(simulator);
        memToReg = 0;
        regWrite = 0;
        memWrite = 0;
        branch = 0;

        branchTargetAddress = 0;
        zero = 0;
        ALUResult = 0;
        readData2 = 0;
        writeAddress = 0;

        memoryData = 0;

        PCSrc = 0;
    }

    @Override
    public void run() {
        if(simulator.getInstructionNumber(3) == Simulator.EMPTY)
        {
            simulator.setInstructionNumber(4, Simulator.EMPTY);
            return;
        }
        if(simulator.getInstructionNumber(3) == Simulator.NOP){
            simulator.setInstructionNumber(4, Simulator.NOP);
            return;
        }
        readFromEXToMEM();

        // find the data memory inputs (address, writeData) where the output is the attribute memoryData
        int address = ALUResult;
        int writeData = readData2;



        // we have to possible operations (read & write), MemWrite signal determines
        if(memWrite == 1){ // write operation
            simulator.getDataMemory().write(address, writeData);
            memoryData = 0;
        }else { // read operation
            memoryData = simulator.getDataMemory().read(address);
        }

        // calculate the PCSrc signal which can be done in IF stage
        PCSrc = (branch == 1 && zero == 1) ? 1 : 0;

        // write values to the next pipe
        simulator.getMemToWb().setRegister("MemToReg", memToReg);
        simulator.getMemToWb().setRegister("RegWrite", regWrite);
        simulator.getMemToWb().setRegister("MemoryData", memoryData);
        simulator.getMemToWb().setRegister("ALUResult", ALUResult);
        simulator.getMemToWb().setRegister("WriteAddress", writeAddress);

        simulator.setInstructionNumber(4, simulator.getInstructionNumber(3));


        // Set Next Instruction for Instruction Fetch
//        if((simulator.getExToMem().getRegister("Branch").getValue() & simulator.getExtoMem().getRegister("Zero").getValue()) == 1)
//        {
//            // Stall in case of branch
//            // 1. Clear IF/ID for next instruction
//            simulator.getIFtoID().setRegister("Instruction", 0);
//            simulator.getIFtoID().setRegister("PC", 0);
//
//            // 2. Clear ID/EX for next instruction
//            simulator.getIDtoEx().setRegister("MemWrite", 0);
//            simulator.getIDtoEx().setRegister("MemRead", 0);
//            simulator.getIDtoEx().setRegister("Branch", 0);
//            simulator.getIDtoEx().setRegister("RegWrite", 0);
//
//            // 3. Clear EX/MEM for next instruction
//            simulator.getExToMem().setRegister("MemWrite", 0);
//            simulator.getExToMem().setRegister("MemRead", 0);
//            simulator.getExToMem().setRegister("Branch", 0);
//            simulator.getExToMem().setRegister("RegWrite", 0);
//
//            simulator.setInstructionNumber(1, Simulator.NOP);
//            simulator.setInstructionNumber(2, Simulator.NOP);
//            simulator.setInstructionNumber(3, Simulator.NOP);
//        }
    }

    /**
     * Reads data from the EX to MEM pipeline register into the Memory stage.
     * This method assigns the respective values of control signals, computation results,
     * and other relevant data from the EX/MEM pipeline register to local variables within
     * the Memory stage for further processing.
     *
     * Extracted values include:
     * - Control signals (MemToReg, RegWrite, MemWrite, Branch)
     * - Branch target address
     * - Zero flag
     * - Result of the ALU operation
     * - Value of the second operand of memory instructions
     * - Write address for register file
     */
    private void readFromEXToMEM() {
        // control signals
        memToReg = simulator.getExToMem().getRegister("MemToReg").getValue();
        regWrite = simulator.getExToMem().getRegister("RegWrite").getValue();
        memWrite = simulator.getExToMem().getRegister("MemWrite").getValue();
        branch = simulator.getExToMem().getRegister("Branch").getValue();

        branchTargetAddress = simulator.getExToMem().getRegister("BranchTargetAddress").getValue();
        zero = simulator.getExToMem().getRegister("Zero").getValue();
        ALUResult = simulator.getExToMem().getRegister("ALUResult").getValue();
        readData2 = simulator.getExToMem().getRegister("ReadData2").getValue();
        writeAddress = simulator.getExToMem().getRegister("WriteAddress").getValue();
    }
}
