package br.com.fiap.mscontribuicoes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "ms-obras", fallbackFactory = ObrasClientFallbackFactory.class)
public interface ObrasClient {

    @PostMapping("/api/v1/obras")
    ObraClientResponse criarObra(@RequestBody ObraClientRequest request);

    @PatchMapping("/api/v1/obras/{id}/status")
    ObraClientResponse alterarStatus(@PathVariable("id") Long id, @RequestBody StatusObraClientRequest request);
}
