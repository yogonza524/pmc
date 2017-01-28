package com.pichon.modulopmc;

import Util.PMC;
import Util.Pesos;
import Util.ProcesoAprendizaje;
import Util.UtilPMC;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import sun.awt.AWTAccessor;

public class FXMLControllerPMC implements Initializable {
    
    PMC redNeuronal;
    SingleSelectionModel<Tab> selectionModel;
    @FXML private TextArea resultado;
    @FXML private Tab topologiaGraf;
    @FXML private TextField capasOcultas;
    @FXML private TextArea patronesEntrada;
    @FXML private CheckBox aleatorio;
    @FXML private TextArea entradasClasificar;
    @FXML private TextField entradas;
    @FXML private TextField perceptronXCapa;
    @FXML private TableView<?> tablaFuncion;
    @FXML private TextArea patronesSalida;
    @FXML private ChoiceBox<String> funcion;
    @FXML private Tab aprendizajeGraf;
    @FXML private TextField salidas;
    @FXML private TableView<?> tablaPesos;
    @FXML private TextField errorAceptable;
    @FXML private TextField valorAlfa;
    @FXML private Tab pestanaAprendizaje;
    @FXML private Tab pestanasFuncion;
    @FXML private Tab pestanaPeso;
    @FXML private StackPane canvasTopologia;
    @FXML private StackPane canvasGrafico;
    @FXML private TabPane tabPanelGraficos;

    @FXML
    void clasificarEntradas(ActionEvent event) {
        resultado.setText(resultado.getText() + "\n - - - - - - - - - - - - - \n Clasificacion de entradas:\n");
        ArrayList<ArrayList<Double>> entradaClasificar = UtilPMC.cargarPuntos(entradasClasificar.getText(), redNeuronal.getCantEntradas());
        for (int i = 0; i < entradaClasificar.size(); i++) {
            String salidaProceso = redNeuronal.runRedNeuronal(entradaClasificar.get(i));
            resultado.setText(resultado.getText()+salidaProceso+"\n");
        }
    }

    //Generar la topologia de la red
    @FXML void generarRed(ActionEvent event) {
        redNeuronal = new PMC();
        //Ingreso datos topologia de la red
        redNeuronal.setCantEntradas(Integer.parseInt(entradas.getText()));
        redNeuronal.setCantCapasOculta(Integer.parseInt(capasOcultas.getText()));
        redNeuronal.setCantNeuronasCapasOcultaI(Integer.parseInt(perceptronXCapa.getText()));
        redNeuronal.setCantSalida(Integer.parseInt(salidas.getText()));
        redNeuronal.setErrorAceptable(Double.valueOf(errorAceptable.getText()));
        redNeuronal.setAlfa(Double.valueOf(valorAlfa.getText()));
        redNeuronal.setFuncion(funcion.getValue());
        resultado.setText(redNeuronal.informeRed() + "\n \n");
        
        //habilitar las otras pestañas
        pestanaAprendizaje.setDisable(false);
        pestanasFuncion.setDisable(false);
        pestanaPeso.setDisable(false);
        
        //Cargar tabla de pesos
        
    }

    @FXML
    void resetTodo(ActionEvent event) {

    }

    @FXML
    void ensenarPatrones(ActionEvent event) {
        ArrayList<ArrayList<Double>> valoresEntrada = UtilPMC.cargarPuntos(patronesEntrada.getText(), redNeuronal.getCantEntradas());
        ArrayList<ArrayList<Double>> valoresSalida = UtilPMC.cargarPuntos(patronesSalida.getText(), redNeuronal.getCantSalida());
        //Ingresar patrones de entrada
        redNeuronal.setValorEntradas(valoresEntrada);
        //Ingresar patrones de salida
        redNeuronal.setValorSalida(valoresSalida);
        redNeuronal.inicializarRedNeuronal();
        
        //Comenzar proceso de aprendizaje
        ProcesoAprendizaje procesoAprend;
        procesoAprend = redNeuronal.entrenarRedNeuronal();
        
        //Aprendizaje terminado
        //mostrar las primeras n iteraciones del proceso. Sino son muchas y al pedo
        int iteraciones = 10;
        for (int i = 0; i < 1+(4*iteraciones); i++) {
            resultado.setText(resultado.getText()+ procesoAprend.getProceso().get(i));
        }
        //Muestro los pesos estabilizados que cumplen con el error aceptable
        resultado.setText(resultado.getText()+ "- - - - - - - - - -\n" + "Pesos estabilizados\n");
        int ultimosCinco = procesoAprend.getProceso().size();
        for (int i = ultimosCinco-5; i < ultimosCinco; i++) {
            resultado.setText(resultado.getText() + procesoAprend.getProceso().get(i) + "\n");
        }
        
        //Graficar error durante aprendizaje
        canvasGrafico.getChildren().clear();
        canvasGrafico.getChildren().add(generarGraficoError(procesoAprend.getErrorEpoca()));
        
    }
    
        @FXML
    void actualizarFuncion(ActionEvent event) {

    }

    @FXML
    void actualizarPeso(ActionEvent event) {

    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        funcion.getItems().addAll(
            "Lineal", 
            "Sigmoidal"
        );
        funcion.setValue("Sigmoidal");
        selectionModel = tabPanelGraficos.getSelectionModel();
    }

    private LineChart<Number, Number> generarGraficoError(ArrayList<Double> procesoAprend){
        selectionModel.select(1); //Selecciono la pestana del grafico
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Interaciones");
        yAxis.setLabel("Error");
        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Grafica del error cometido");
        XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
        series.setName("Error cometido");
        
        //Agregar cada punto al grafico
        for (int i = 0; i < procesoAprend.size(); i=i+5) {
            series.getData().add(new XYChart.Data<Number, Number>(i, procesoAprend.get(i)));
        }
        lineChart.getData().add(series);
        lineChart.setCreateSymbols(false);
        return lineChart;
    }
}
