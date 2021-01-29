//
// Created by Vardan Gurjyan on 1/28/21.
//

#include "tridas-service.hpp"
namespace clara {
    namespace tridas {

        TriDASService::~TriDASService() {

        }

        TriDASService::TriDASService() {

        }

        std::string clara::tridas::TriDASService::name() const {
            return std::string();
        }

        std::string clara::tridas::TriDASService::author() const {
            return std::string();
        }

        std::string clara::tridas::TriDASService::description() const {
            return std::string();
        }

        std::string clara::tridas::TriDASService::version() const {
            return std::string();
        }

        void clara::tridas::TriDASService::connect(int stream_port, const json11::Json &opts) {

        }

        void clara::tridas::TriDASService::disconnect() {

        }

        clara::any clara::tridas::TriDASService::process_frame(int event_number) {
            return clara::any();
        }

        clara::stdlib::StreamingService::Endian clara::tridas::TriDASService::get_byte_order() {
            return clara::stdlib::StreamingService::Endian::Little;
        }

        const clara::EngineDataType &clara::tridas::TriDASService::get_data_type() const {
            return clara::type::STRING;

        }

    }
}
