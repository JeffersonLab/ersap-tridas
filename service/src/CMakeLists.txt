# Create one shared library per service
set(TRIDAS_SERVICES
        tridas-service
        )

foreach(service ${TRIDAS_SERVICES})
    add_library(${service} SHARED ${service}.cpp)
    target_link_libraries(${service}  Clara::clara)
#    target_link_libraries(${service} tridas_data tridas_engine Clara::clara)
    if (CMAKE_CXX_COMPILER_ID MATCHES Clang)
        target_compile_options(${service} PRIVATE -Wno-return-type-c-linkage)
    endif()
endforeach()

install(TARGETS ${TRIDAS_SERVICES} DESTINATION ${CMAKE_INSTALL_PLUGINDIR}/tridas/service)
