package util.faltas;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CalculoCargaHoraria {
    public static final int MINUTOS_POR_AULA = 50;

    private CalculoCargaHoraria() { }

    public static int totalAulas(int cargaHorariaEmHoras) {
        if (cargaHorariaEmHoras <= 0) return 0;
        long minutos = (long) cargaHorariaEmHoras * 60;
        long aulas = (minutos + MINUTOS_POR_AULA - 1) / MINUTOS_POR_AULA;
        if (aulas > Integer.MAX_VALUE) throw new IllegalArgumentException("Carga horária muito grande.");
        return (int) aulas;
    }

    public static int aulasEmIntervalo(int minutos) {
        if (minutos < MINUTOS_POR_AULA) return 0;
        return minutos / MINUTOS_POR_AULA;
    }

    public static int limiteFaltas(int cargaHorariaEmHoras, BigDecimal percentual) {
        int aulas = totalAulas(cargaHorariaEmHoras);
        if (aulas == 0 || percentual == null || percentual.signum() < 0
                || percentual.compareTo(BigDecimal.valueOf(100)) > 0) return 0;
        return BigDecimal.valueOf(aulas).multiply(percentual)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
    }

    public static int semanasEstimadas(int cargaHorariaEmHoras, int aulasPorSemana) {
        if (aulasPorSemana <= 0) return 0;
        int aulas = totalAulas(cargaHorariaEmHoras);
        return (aulas + aulasPorSemana - 1) / aulasPorSemana;
    }
}
