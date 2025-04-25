package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Entities.History;

public interface Command {
    History execute();
    void undo();
}