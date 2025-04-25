package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Entities.History;
import org.springframework.stereotype.Service;

import java.util.Stack;
@Service
public class CommandInvoker {
    private final Stack<Command> commandHistory = new Stack<>();

    public History executeCommand(Command command) {
        History history = command.execute();
        commandHistory.push(command);
        return history;
    }

    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.pop();
            lastCommand.undo();
        }
    }
}