using Habitus.Stats;

var builder = WebApplication.CreateBuilder(args);

builder.Services.Configure<StatsOptions>(builder.Configuration.GetSection("Stats"));
builder.Services.AddCors((options) =>
{
    options.AddDefaultPolicy((policy) =>
    {
        policy
            .WithOrigins("http://localhost:5173", "http://localhost:3000")
            .AllowAnyHeader()
            .AllowAnyMethod();
    });
});
builder.Services.AddSingleton<TokenReader>();
builder.Services.AddScoped<StatsService>();

var app = builder.Build();

app.UseCors();

app.MapGet("/api/stats/evolution", async (
    HttpRequest request,
    StatsService statsService,
    CancellationToken cancellationToken,
    int days = 30) =>
{
    try
    {
        if (!request.Headers.TryGetValue("Authorization", out var authorization))
        {
            return Results.Unauthorized();
        }

        var result = await statsService.GetEvolutionAsync(authorization.ToString(), days, cancellationToken);
        return Results.Ok(result);
    }
    catch (UnauthorizedAccessException)
    {
        return Results.Unauthorized();
    }
    catch (ArgumentException exception)
    {
        return Results.BadRequest(new { message = exception.Message });
    }
});

app.MapGet("/api/stats/health", () => Results.Ok(new { status = "ok", service = "habitus-stats" }));

app.Run();
