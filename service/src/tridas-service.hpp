//
// Created by Vardan Gurjyan on 1/28/21.
//

#ifndef ERSAP_TRIDAS_TRIDASSERVICE_HPP
#define ERSAP_TRIDAS_TRIDASSERVICE_HPP

#include <clara/stdlib/streaming_service.hpp>

namespace clara {
    namespace tridas {
        class TriDASService : public clara::stdlib::StreamingService {

        public:
            TriDASService();
            ~TriDASService() override;

        public:
            std::string name() const override;

            std::string author() const override;

            std::string description() const override;

            std::string version() const override;

        private:
            void connect(int stream_port, const json11::Json &opts) override;

            void disconnect() override;

            clara::any process_frame(int event_number) override;

            Endian get_byte_order() override;

            const clara::EngineDataType &get_data_type() const override;
        };

    } // end namespace tridas
} // end namespace clara
#endif //ERSAP_TRIDAS_TRIDASSERVICE_HPP
