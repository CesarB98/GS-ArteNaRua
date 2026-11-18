package br.com.fiap.mscontribuicoes.client;

public record ObraClientRequest(String titulo, Integer ano, String historia, String audiodescricao,
                                Double latitude, Double longitude, String endereco, String tipo,
                                Long artistaId) {
}
