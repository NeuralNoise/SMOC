/*
TASK: aplusb
LANG: C
*/
// TRIES TO CREATE SOCKET, works ok if unable
#include<stdio.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<arpa/inet.h>
#include<unistd.h>

int main(int argc, char *argv[])
{   int a,b;

    int server_sockfd;
    int server_len;
    struct sockaddr_in server_address;
    /*  Create an unnamed socket for the server.   */
    server_sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_sockfd!=-1)
    {   /*  Name the socket.   */
        server_address.sin_family = AF_INET;
        /* Point interface (INADDR_ANY if any) */
        server_address.sin_addr.s_addr = inet_addr("0.0.0.0");
        server_address.sin_port = 51234;
        server_len = sizeof(server_address);
        if (bind(server_sockfd, (struct sockaddr *)&server_address, server_len)!=-1)
        {   /*   Create a connection queue and wait for clients. */
            if (listen(server_sockfd, 5)!=-1);
            {   printf("!!!ABLE TO CREATE SOCKET!!!");
                return 0;
            }
        }
    }
    scanf("%d %d", &a, &b);
    printf("%d\n", a+b);
    return 0;
}
