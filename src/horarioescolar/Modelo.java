/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package horarioescolar;

import ilog.concert.*;
import ilog.cplex.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author a11030
 */
public class Modelo {

    private int quantidadeProfessor;
    private int quantidadeTurma;
    private int quantidadeDias;
    private int quantidadeHorario;
    private Map<Integer, String> professores;
    private Map<String, Integer> professoresNome;
    private Map<Integer, String> turmas;
    private Map<String, Integer> turmasNome;
    private Map<Integer, String> dias;
    private Map<String, Integer> diasNome;
    private Map<Integer, String> horarios;
    private Map<String, Integer> horariosNome;
    private int[][] necessidade;
    private int[][][] indisponibilidade;

    public boolean readFile(String nomeArquivo) {
        try {
            FileReader fileReader = new FileReader(new File(nomeArquivo));
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            if (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                this.quantidadeProfessor = Integer.parseInt(linha);
            }
            if (bufferedReader.ready()) {
                String[] linha = bufferedReader.readLine().split(", ");
                professores = new HashMap<>(quantidadeProfessor);
                professoresNome = new HashMap<>(quantidadeProfessor);
                for (int i = 0; i < quantidadeProfessor; i++) {
                    professores.put(i, linha[i]);
                    professoresNome.put(linha[i], i);
                }
            }
            if (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                this.quantidadeTurma = Integer.parseInt(linha);
            }
            if (bufferedReader.ready()) {
                String[] linha = bufferedReader.readLine().split(", ");
                turmas = new HashMap<>(quantidadeTurma);
                turmasNome = new HashMap<>(quantidadeTurma);
                for (int i = 0; i < quantidadeTurma; i++) {
                    turmas.put(i, linha[i]);
                    turmasNome.put(linha[i], i);
                }
            }
            if (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                this.quantidadeDias = Integer.parseInt(linha);
            }
            if (bufferedReader.ready()) {
                String[] linha = bufferedReader.readLine().split(", ");
                dias = new HashMap<>(quantidadeDias);
                diasNome = new HashMap<>(quantidadeDias);
                for (int i = 0; i < quantidadeDias; i++) {
                    dias.put(i, linha[i]);
                    diasNome.put(linha[i], i);
                }
            }
            if (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                this.quantidadeHorario = Integer.parseInt(linha);
            }
            if (bufferedReader.ready()) {
                String[] linha = bufferedReader.readLine().split(", ");
                horarios = new HashMap<>(quantidadeHorario);
                horariosNome = new HashMap<>(quantidadeHorario);
                for (int i = 0; i < quantidadeHorario; i++) {
                    horarios.put(i, linha[i]);
                    horariosNome.put(linha[i], i);
                }
            }
            necessidade = new int[quantidadeProfessor][quantidadeTurma];
            if (bufferedReader.ready()) {
                String linha = bufferedReader.readLine();
                if (linha.equals("Necessidade")) {
                    while (bufferedReader.ready()) {
                        linha = bufferedReader.readLine();
                        if (linha.equals("Indisponibilidade")) {
                            break;
                        }
                        String[] necessidadeTempStrings = linha.split(" => ");
                        String[] necessidadeProfessorTurmaStrings = necessidadeTempStrings[0].split(", ");
                        int professor = professoresNome.get(necessidadeProfessorTurmaStrings[0]);
                        int turma = turmasNome.get(necessidadeProfessorTurmaStrings[1]);
                        necessidade[professor][turma] = Integer.parseInt(necessidadeTempStrings[1]);
                    }
                }
            }
            indisponibilidade = new int[quantidadeDias][quantidadeHorario][quantidadeProfessor];
            if (bufferedReader.ready()) {
                while (bufferedReader.ready()) {
                    String linha = bufferedReader.readLine();
                    String[] indisponibilidadeTempStrings = linha.split(" => ");
                    String[] indisponibilidadeDiaAula = indisponibilidadeTempStrings[0].split(", ");
                    int dia = diasNome.get(indisponibilidadeDiaAula[0]);
                    int horario = horariosNome.get(indisponibilidadeDiaAula[1]);
                    int professor = professoresNome.get(indisponibilidadeTempStrings[1]);
                    indisponibilidade[dia][horario][professor] = 1;
                }
            }
            bufferedReader.close();
            fileReader.close();
            return true;
        } catch (IOException | NumberFormatException ex) {
            System.out.println("Arquivo " + nomeArquivo + " não foi possível ler.");
            System.err.print("Concert exception caught: " + ex);
            return false;
        }
    }

    public void modelo() {
        try {
            IloCplex model = new IloCplex();

            /**
             * Iniciando as variaveis booleanas
             */
            IloNumVar[][][][] X = new IloNumVar[quantidadeDias][quantidadeHorario][quantidadeTurma][quantidadeProfessor];
            for (int dia = 0; dia < quantidadeDias; dia++) {
                for (int horario = 0; horario < quantidadeHorario; horario++) {
                    for (int turma = 0; turma < quantidadeTurma; turma++) {
                        for (int professor = 0; professor < quantidadeProfessor; professor++) {
                            X[dia][horario][turma][professor] = model.boolVar();
                        }
                    }
                }
            }
            /**
             * Função objetivo
             */
            IloLinearNumExpr objective = model.linearNumExpr();
            for (int dia = 0; dia < quantidadeDias; dia++) {
                for (int horario = 0; horario < quantidadeHorario; horario++) {
                    for (int turma = 0; turma < quantidadeTurma; turma++) {
                        for (int professor = 0; professor < quantidadeProfessor; professor++) {
                            objective.addTerm(1.0, X[dia][horario][turma][professor]);
                        }
                    }
                }
            }
            model.addMaximize(objective);

            /**
             * Necessidade t,p E Z+, para todo t,p
             */
            for (int turma = 0; turma < quantidadeTurma; turma++) {
                for (int professor = 0; professor < quantidadeProfessor; professor++) {
                    IloLinearNumExpr expr = model.linearNumExpr();
                    for (int dia = 0; dia < quantidadeDias; dia++) {
                        for (int horario = 0; horario < quantidadeHorario; horario++) {
                            expr.addTerm(1.0, X[dia][horario][turma][professor]);
                        }
                    }
                    model.addEq(expr, necessidade[professor][turma]);
                }
            }
            /**
             * Professor não pode estar em mais de uma turma ao mesmo tempo
             */
            for (int professor = 0; professor < quantidadeProfessor; professor++) {
                for (int horario = 0; horario < quantidadeHorario; horario++) {
                    for (int dia = 0; dia < quantidadeDias; dia++) {
                        IloLinearNumExpr expr = model.linearNumExpr();
                        for (int turma = 0; turma < quantidadeTurma; turma++) {
                            expr.addTerm(1.0, X[dia][horario][turma][professor]);
                        }
                        model.addLe(expr, 1.0);
                    }
                }
            }
            /**
             * Turma não pode ter o mesmo professor no mesmo dia e horario
             */
            for (int turma = 0; turma < quantidadeTurma; turma++) {
                for (int horario = 0; horario < quantidadeHorario; horario++) {
                    for (int dia = 0; dia < quantidadeDias; dia++) {
                        IloLinearNumExpr expr = model.linearNumExpr();
                        for (int professor = 0; professor < quantidadeProfessor; professor++) {
                            expr.addTerm(1.0, X[dia][horario][turma][professor]);
                        }
                        model.addLe(expr, 1.0);
                    }
                }
            }
            /**
             * Professor não da mais de duas horas aula por dia
             */
            for (int turma = 0; turma < quantidadeTurma; turma++) {
                for (int dia = 0; dia < quantidadeDias; dia++) {
                    for (int professor = 0; professor < quantidadeProfessor; professor++) {
                        IloLinearNumExpr expr = model.linearNumExpr();
                        for (int horario = 0; horario < quantidadeHorario; horario++) {
                            expr.addTerm(1.0, X[dia][horario][turma][professor]);
                        }
                        model.addLe(expr, 2.0);
                    }
                }
            }
            /**
             * Indisponibilidade dia x horario x professor E {0,1}, para todo
             * dia,horario,professor 1 não pode dar aula 0 pode dar aula
             */
            for (int dia = 0; dia < quantidadeDias; dia++) {
                for (int horario = 0; horario < quantidadeHorario; horario++) {
                    for (int professor = 0; professor < quantidadeProfessor; professor++) {
                        if (indisponibilidade[dia][horario][professor] == 1) {
                            IloLinearNumExpr expr = model.linearNumExpr();
                            for (int turma = 0; turma < quantidadeTurma; turma++) {
                                expr.addTerm(1.0, X[dia][horario][turma][professor]);
                            }
                            model.addEq(expr, 0.0);
                        }
                    }
                }
            }

            if (model.solve()) {
                System.out.println("Status = " + model.getStatus());
                System.out.println("Value = " + model.getBestObjValue());

                //getRelatorioTurmas(model, X);
                //getRelatorioProfessores(model, X);
            } else {
                System.out.println("A feasible solution may still be present, but IloCplex has not been able to prove its feasibility.");
            }
        } catch (IloException exception) {
            System.err.print("Concert exception caught: " + exception);
        }
    }

    private void getRelatorioTurmas(IloCplex model, IloNumVar[][][][] X) throws IloException {
        try {
            for (int turma = 0; turma < quantidadeTurma; turma++) {
                String nomeArquivo = "turmas/" + turmas.get(turma) + ".txt";
                FileWriter fileWriter = new FileWriter(new File(nomeArquivo));
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(turmas.get(turma));
                bufferedWriter.newLine();
                bufferedWriter.write("---------------------------------------------------------------------------------------------");
                bufferedWriter.newLine();
                bufferedWriter.write("\t\t");
                for (int dia = 0; dia < quantidadeDias; dia++) {
                    bufferedWriter.write(dias.get(dia) + "\t\t");
                }
                bufferedWriter.newLine();
                bufferedWriter.write("---------------------------------------------------------------------------------------------");
                bufferedWriter.newLine();
                for (int horario = 0; horario < quantidadeHorario; horario++) {
                    bufferedWriter.write("|" + horarios.get(horario) + "\t");
                    for (int dia = 0; dia < quantidadeDias; dia++) {
                        for (int professor = 0; professor < quantidadeProfessor; professor++) {
                            if (model.getValue(X[dia][horario][turma][professor]) == 1.0) {
                                bufferedWriter.write("|" + professores.get(professor) + "\t\t");
                            }
                        }
                        bufferedWriter.write("");
                    }
                    bufferedWriter.newLine();
                    bufferedWriter.write("---------------------------------------------------------------------------------------------");
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
                bufferedWriter.close();
                fileWriter.close();
            }
        } catch (IOException ex) {
            System.err.print("Concert exception caught: " + ex);
        }
    }

    private void getRelatorioProfessores(IloCplex model, IloNumVar[][][][] X) throws IloException {
        try {
            for (int professor = 0; professor < quantidadeProfessor; professor++) {
                String nomeArquivo = "professores/" + professores.get(professor) + ".txt";
                FileWriter fileWriter = new FileWriter(new File(nomeArquivo));
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(professores.get(professor));
                bufferedWriter.newLine();
                bufferedWriter.write("---------------------------------------------------------------------------------------------");
                bufferedWriter.newLine();
                bufferedWriter.newLine();
                bufferedWriter.write("---------------------------------------------------------------------------------------------");
                bufferedWriter.newLine();
                bufferedWriter.write("\t");
                for (int dia = 0; dia < quantidadeDias; dia++) {
                    bufferedWriter.write("\t" + dias.get(dia) + "\t");
                }
                bufferedWriter.newLine();
                bufferedWriter.write("---------------------------------------------------------------------------------------------");
                bufferedWriter.newLine();
                for (int horario = 0; horario < quantidadeHorario; horario++) {
                    bufferedWriter.write("|" + horarios.get(horario) + "\t");
                    for (int dia = 0; dia < quantidadeDias; dia++) {
                        for (int turma = 0; turma < quantidadeTurma; turma++) {
                            if (model.getValue(X[dia][horario][turma][professor]) == 1.0) {
                                bufferedWriter.write("|" + turmas.get(turma) + "\t");
                            }
                        }
                    }
                    bufferedWriter.newLine();
                    bufferedWriter.write("---------------------------------------------------------------------------------------------");
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
                bufferedWriter.close();
                fileWriter.close();
            }
        } catch (IOException ex) {
            System.err.print("Concert exception caught: " + ex);
        }
    }

}
